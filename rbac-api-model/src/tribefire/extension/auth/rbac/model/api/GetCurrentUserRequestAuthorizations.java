package tribefire.extension.auth.rbac.model.api;

import com.braintribe.model.generic.eval.EvalContext;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.service.api.ServiceRequest;

import tribefire.extension.auth.rbac.model.api.data.CurrentUserRequestAuthorizations;

public interface GetCurrentUserRequestAuthorizations extends ServiceAuthorizationReflectionRequest {
	EntityType<GetCurrentUserRequestAuthorizations> T = EntityTypes.T(GetCurrentUserRequestAuthorizations.class);
	
	@Override
	EvalContext<? extends CurrentUserRequestAuthorizations> eval(Evaluator<ServiceRequest> evaluator);
}

