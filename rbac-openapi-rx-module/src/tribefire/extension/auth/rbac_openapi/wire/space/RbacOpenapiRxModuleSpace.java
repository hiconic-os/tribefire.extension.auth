package tribefire.extension.auth.rbac_openapi.wire.space;

import static com.braintribe.wire.api.util.Sets.set;

import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

import hiconic.rx.module.api.wire.RxModuleContract;
import hiconic.rx.openapi.v3.api.OpenapiV3Contract;
import tribefire.extension.auth.rbac.processing.ServiceRequestAuthorizationResolver;
import tribefire.extension.auth.rbac_openapi.processing.RbacRxOpenapiDescriptionResolver;

@Managed
public class RbacOpenapiRxModuleSpace implements RxModuleContract {

	@Import
	private OpenapiV3Contract openapi;

	@Override
	public void onDeploy() {
		openapi.descriptionResolverRegistry().registerDescriptionResolver("auth", openapiDescriptionResolver());
	}

	@Managed
	private RbacRxOpenapiDescriptionResolver openapiDescriptionResolver() {
		RbacRxOpenapiDescriptionResolver bean = new RbacRxOpenapiDescriptionResolver();
		bean.setAuthorizationResolver(serviceRequestAuthorizationResolver());
		return bean;
	}

	@Managed
	private ServiceRequestAuthorizationResolver serviceRequestAuthorizationResolver() {
		return new ServiceRequestAuthorizationResolver(set("internal"));
	}
}
