package tribefire.extension.auth.rbac.processing;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.processing.meta.cmd.CmdResolver;
import com.braintribe.model.processing.meta.cmd.builders.EntityMdResolver;
import com.braintribe.model.service.api.AuthorizedRequest;

import tribefire.extension.auth.rbac.model.meta.AccessControl;
import tribefire.extension.auth.rbac.model.meta.AllowRoles;
import tribefire.extension.auth.rbac.model.meta.DenyRoles;
import tribefire.extension.auth.rbac.model.meta.InduceRoles;
import tribefire.extension.auth.rbac.model.meta.OverrideRoles;

public class ServiceRequestAuthorizationResolver {
	private Set<String> overrideRoles;
	
	public ServiceRequestAuthorizationResolver(Set<String> overrideRoles) {
		this.overrideRoles = overrideRoles;
	}

	private static class RoleAggregator {
		private int mdAdded = 0;
		private Set<String> roles = Collections.emptySet();
		
		public void add(AccessControl ac) {
			Set<String> newRoles = ac.getRoles();
			
			if (newRoles.isEmpty())
				return;
			
			switch (mdAdded) {
				case 0 -> roles = newRoles;
				case 1 -> {
					roles = new HashSet<>(roles);
					roles.addAll(newRoles);
				}
				default -> roles.addAll(newRoles);
			}
			
			mdAdded++;
		}
		
		public Set<String> getRoles() {
			return roles;
		}
	}
	
	public ServiceAuthorization resolve(EntityMdResolver entityMdResolver) {
		RoleAggregator mdOverrideRoles = new RoleAggregator();
		RoleAggregator allowRoles = new RoleAggregator();
		RoleAggregator denyRoles = new RoleAggregator();
		
		List<AccessControl> accessControls = entityMdResolver.meta(AccessControl.T).list();
		
		for (AccessControl accessControl: accessControls) {
				switch (accessControl) {
				case AllowRoles ar -> allowRoles.add(ar);
				case DenyRoles dr -> denyRoles.add(dr);
				case OverrideRoles or -> mdOverrideRoles.add(or);
				default -> {}
			}
		}

		Set<String> effectiveOverrideRoles = union(overrideRoles, mdOverrideRoles.getRoles());
		return new ServiceAuthorization(entityMdResolver.getGmEntityType(), effectiveOverrideRoles, allowRoles.getRoles(), denyRoles.getRoles());
	}

	public Set<String> resolveInducedRoles(EntityMdResolver entityMdResolver) {
		Set<String> roles = Collections.emptySet();
		for (InduceRoles metadata : entityMdResolver.meta(InduceRoles.T).list())
			roles = union(roles, metadata.getRoles());
		return roles;
	}

	private static Set<String> union(Set<String> first, Set<String> second) {
		if (first.isEmpty())
			return second;
		if (second.isEmpty())
			return first;
		Set<String> result = new HashSet<>(first);
		result.addAll(second);
		return result;
	}
	

}
