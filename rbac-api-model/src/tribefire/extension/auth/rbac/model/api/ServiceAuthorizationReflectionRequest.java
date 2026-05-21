package tribefire.extension.auth.rbac.model.api;

import com.braintribe.model.generic.annotation.Abstract;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.service.api.AuthorizedRequest;
import com.braintribe.model.service.api.DomainRequest;

@Abstract
public interface ServiceAuthorizationReflectionRequest extends AuthorizedRequest, DomainRequest {
	EntityType<ServiceAuthorizationReflectionRequest> T = EntityTypes.T(ServiceAuthorizationReflectionRequest.class);
}

