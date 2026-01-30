package tribefire.extension.auth.test.model;

import com.braintribe.model.generic.annotation.Abstract;
import com.braintribe.model.generic.eval.EvalContext;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.service.api.AuthorizedRequest;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.model.service.api.result.Neutral;

@Abstract
public interface TestRequest extends AuthorizedRequest {

	EntityType<TestRequest> T = EntityTypes.T(TestRequest.class);
	
	@Override
	EvalContext<Neutral> eval(Evaluator<ServiceRequest> evaluator);
}
