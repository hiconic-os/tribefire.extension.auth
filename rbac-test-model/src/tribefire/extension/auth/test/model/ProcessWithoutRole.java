package tribefire.extension.auth.test.model;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

public interface ProcessWithoutRole extends TestRequest {

	EntityType<ProcessWithoutRole> T = EntityTypes.T(ProcessWithoutRole.class);
}
