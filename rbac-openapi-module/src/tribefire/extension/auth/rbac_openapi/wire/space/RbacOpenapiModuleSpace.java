
package tribefire.extension.auth.rbac_openapi.wire.space;

import static com.braintribe.wire.api.util.Sets.set;

import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

import tribefire.extension.auth.rbac.processing.ServiceRequestAuthorizationResolver;
import tribefire.extension.auth.rbac_openapi.processing.RbacOpenapiDescriptionResolver;
import tribefire.extension.webapi.openapi_v3.api.wire.contract.OpenapiV3ModuleContract;
import tribefire.module.wire.contract.TribefireModuleContract;
import tribefire.module.wire.contract.TribefireWebPlatformContract;

/**
 * This module's javadoc is yet to be written.
 */
@Managed
public class RbacOpenapiModuleSpace implements TribefireModuleContract {

	@Import
	private TribefireWebPlatformContract tfPlatform;

	@Import
	private OpenapiV3ModuleContract openapi;
	
	@Override
	public void bindHardwired() {
		// Bind hardwired deployables here.
		openapi.descriptionResolverRegistry().registerDescriptionResolver("auth", openApiDescriptionResolver());
	}
	
	@Managed
	private RbacOpenapiDescriptionResolver openApiDescriptionResolver() {
		var bean = new RbacOpenapiDescriptionResolver();
		bean.setAuthorizationResolver(serviceRequestAuthorizationResolver());
		return bean;
	}
	
	@Managed
	private ServiceRequestAuthorizationResolver serviceRequestAuthorizationResolver() {
		return new ServiceRequestAuthorizationResolver(set("tf-internal"));
	}
}