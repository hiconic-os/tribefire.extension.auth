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

public class ServiceAuthorizationResolver {
	private String domainId;
	private CmdResolver mdResolver;
	private Set<String> overrideRoles;
	
	public ServiceAuthorizationResolver(String domainId, CmdResolver mdResolver, Set<String> overrideRoles) {
		super();
		this.domainId = domainId;
		this.mdResolver = mdResolver;
		this.overrideRoles = overrideRoles;
	}

	public Set<EntityType<? extends AuthorizedRequest>> getRequestTypes() {
		Set<EntityType<? extends AuthorizedRequest>> requestTypes = mdResolver.getModelOracle().findEntityTypeOracle(AuthorizedRequest.T) //
				.getSubTypes().transitive().onlyInstantiable().asTypes();
		return requestTypes;
	}
	
	public Set<ServiceAuthorization> resolveAll() {
		Set<ServiceAuthorization> authorizations = new HashSet<>();
		
		for (var requestType : getRequestTypes()) {
			authorizations.add(resolve(requestType));
		}
		
		return authorizations;
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
	
	public ServiceAuthorization resolve(EntityType<? extends AuthorizedRequest> requestType) {
		EntityMdResolver entityMdResolver = mdResolver.getMetaData().entityType(requestType);
		
		RoleAggregator allowRoles = new RoleAggregator();
		RoleAggregator denyRoles = new RoleAggregator();
		
		List<AccessControl> accessControls = entityMdResolver.meta(AccessControl.T).list();
		
		for (AccessControl accessControl: accessControls) {
			switch (accessControl) {
				case AllowRoles ar -> allowRoles.add(ar);
				case DenyRoles dr -> denyRoles.add(dr);
				default -> {}
			}
		}
		
		return new ServiceAuthorization(domainId, requestType, overrideRoles, allowRoles.getRoles(), denyRoles.getRoles());
	}
	

}
