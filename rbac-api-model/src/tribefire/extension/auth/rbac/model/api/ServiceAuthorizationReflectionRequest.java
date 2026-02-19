package tribefire.extension.auth.rbac.model.api;

import com.braintribe.model.generic.annotation.Abstract;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.service.api.AuthorizedRequest;

@Abstract
public interface ServiceAuthorizationReflectionRequest extends AuthorizedRequest {
	EntityType<ServiceAuthorizationReflectionRequest> T = EntityTypes.T(ServiceAuthorizationReflectionRequest.class);
}

