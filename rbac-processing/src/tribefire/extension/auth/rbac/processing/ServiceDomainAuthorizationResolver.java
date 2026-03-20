package tribefire.extension.auth.rbac.processing;

import java.util.HashSet;
import java.util.Set;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.processing.meta.cmd.CmdResolver;
import com.braintribe.model.processing.meta.cmd.builders.EntityMdResolver;
import com.braintribe.model.service.api.AuthorizedRequest;

public class ServiceDomainAuthorizationResolver extends ServiceRequestAuthorizationResolver {
	private CmdResolver mdResolver;
	
	public ServiceDomainAuthorizationResolver(CmdResolver mdResolver, Set<String> overrideRoles) {
		super(overrideRoles);
		this.mdResolver = mdResolver;
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
	
	public ServiceAuthorization resolve(EntityType<? extends AuthorizedRequest> requestType) {
		EntityMdResolver entityMdResolver = mdResolver.getMetaData().entityType(requestType);
		return resolve(entityMdResolver);
	}
}
