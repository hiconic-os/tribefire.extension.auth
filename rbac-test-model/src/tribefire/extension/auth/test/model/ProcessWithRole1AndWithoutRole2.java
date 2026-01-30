package tribefire.extension.auth.test.model;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

import tribefire.extension.auth.rbac.annotation.DeniedFor;
import tribefire.extension.auth.rbac.annotation.GrantedFor;

@GrantedFor("role1")
@DeniedFor("role2")
public interface ProcessWithRole1AndWithoutRole2 extends TestRequest {

	EntityType<ProcessWithRole1AndWithoutRole2> T = EntityTypes.T(ProcessWithRole1AndWithoutRole2.class);
}
