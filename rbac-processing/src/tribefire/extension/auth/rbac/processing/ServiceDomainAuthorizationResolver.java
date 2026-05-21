package tribefire.extension.auth.rbac.processing;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.Model;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.processing.meta.cmd.CmdResolver;
import com.braintribe.model.processing.meta.cmd.builders.EntityMdResolver;
import com.braintribe.model.processing.meta.oracle.EntityTypeOracle;
import com.braintribe.model.service.api.AuthorizedRequest;
import com.braintribe.model.service.api.CompositeRequest;
import com.braintribe.model.service.api.ExecuteAuthorized;
import com.braintribe.model.service.api.ExecuteInDomain;
import com.braintribe.model.service.api.ServiceRequest;

public class ServiceDomainAuthorizationResolver extends ServiceRequestAuthorizationResolver {
	private CmdResolver mdResolver;
	private static Set<EntityType<? extends ServiceRequest>> publicServiceApiModelRequests = Set.of(
			CompositeRequest.T,
			ExecuteInDomain.T,
			ExecuteAuthorized.T
	);
	
	public ServiceDomainAuthorizationResolver(CmdResolver mdResolver, Set<String> overrideRoles) {
		super(overrideRoles);
		this.mdResolver = mdResolver;
	}

	public Set<EntityTypeOracle> getRequestTypes() {
		Set<EntityTypeOracle> requestTypes = new HashSet<>(mdResolver.getModelOracle().findEntityTypeOracle(ServiceRequest.T) //
				.getSubTypes().transitive().onlyInstantiable().asEntityTypeOracles());
		
		Model serviceApiModel = ServiceRequest.T.getModel();
		
		for (var it = requestTypes.iterator(); it.hasNext();) {
			var typeOracle = it.next();
			
			if (typeOracle.asType().getModel() == serviceApiModel && !publicServiceApiModelRequests.contains(typeOracle.asType()) )
				it.remove();
		}
		
		return requestTypes;
	}
	
	public Set<ServiceAuthorization> resolveAll() {
		Set<ServiceAuthorization> authorizations = new HashSet<>();
		
		for (var requestTypeOracle : getRequestTypes()) {
			EntityType<? extends ServiceRequest> requestType = requestTypeOracle.asType();
			if (AuthorizedRequest.T.isAssignableFrom(requestType)) {
				EntityType<? extends AuthorizedRequest> authType = (EntityType<? extends AuthorizedRequest>)requestType;
				authorizations.add(resolve(authType));
			}
			else {
				GmEntityType gmEntityType = requestTypeOracle.asGmEntityType();
				authorizations.add(new ServiceAuthorization(gmEntityType, Collections.emptySet(), Collections.emptySet(), Collections.emptySet()));
			}
		}
		
		return authorizations;
	}
	
	public ServiceAuthorization resolve(EntityType<? extends AuthorizedRequest> requestType) {
		EntityMdResolver entityMdResolver = mdResolver.getMetaData().entityType(requestType);
		return resolve(entityMdResolver);
	}
}
