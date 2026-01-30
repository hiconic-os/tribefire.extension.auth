package tribefire.extension.auth.test.wire.space;

import com.braintribe.model.service.api.result.Neutral;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

import hiconic.rx.module.api.service.ServiceDomainConfiguration;
import hiconic.rx.module.api.wire.RxModuleContract;
import hiconic.rx.module.api.wire.RxPlatformContract;
import tribefire.extension.auth._RbacConfiguredApiModel_;
import tribefire.extension.auth.test.model.TestRequest;

/**
 * This module's javadoc is yet to be written.
 */
@Managed
public class RbacRxModuleTestSpace implements RxModuleContract {

	@Import
	private RxPlatformContract platform;

	@Override
	public void configureMainServiceDomain(ServiceDomainConfiguration configuration) {
		configuration.addModel(platform.configuredModels().byName(_RbacConfiguredApiModel_.name));
		configuration.bindRequest(TestRequest.T, () -> (_,_) -> Neutral.NEUTRAL);
	}
}