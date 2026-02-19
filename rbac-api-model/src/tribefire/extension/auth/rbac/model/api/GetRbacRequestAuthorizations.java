package tribefire.extension.auth.rbac.model.api;

import com.braintribe.model.generic.eval.EvalContext;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.service.api.ServiceRequest;

import tribefire.extension.auth.rbac.annotation.AllowRoles;
import tribefire.extension.auth.rbac.model.api.data.RbacRequestAuthorizations;

@AllowRoles({"internal", "tf-internal", "admin", "tf-admin"})
public interface GetRbacRequestAuthorizations extends ServiceAuthorizationReflectionRequest {

	EntityType<GetRbacRequestAuthorizations> T = EntityTypes.T(GetRbacRequestAuthorizations.class);
	
	@Override
	EvalContext<? extends RbacRequestAuthorizations> eval(Evaluator<ServiceRequest> evaluator);
}

