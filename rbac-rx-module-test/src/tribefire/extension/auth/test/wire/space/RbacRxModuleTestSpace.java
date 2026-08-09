package tribefire.extension.auth.test.wire.space;

import org.assertj.core.api.Assertions;

import com.braintribe.common.attribute.common.UserInfoAttribute;
import com.braintribe.model.processing.service.common.context.UserSessionAspect;
import com.braintribe.model.service.api.result.Neutral;
import com.braintribe.utils.collection.impl.AttributeContexts;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

import hiconic.rx.module.api.service.ServiceDomainConfiguration;
import hiconic.rx.module.api.wire.RxModuleContract;
import hiconic.rx.module.api.wire.RxPlatformContract;
import tribefire.extension.auth._RbacConfiguredApiModel_;
import tribefire.extension.auth.test.model.TestRequest;
import tribefire.extension.auth.test.model.ProcessWithInducedRole;
import tribefire.extension.auth.test.model.ProcessWithRole2;

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
		configuration.bindRequest(TestRequest.T, () -> (context, request) -> {
			if (request instanceof ProcessWithInducedRole)
				return ProcessWithRole2.T.create().eval(context).get();
			if (request instanceof ProcessWithRole2) {
				Assertions.assertThat(context.getAttribute(UserSessionAspect.class).getEffectiveRoles()).contains("role2");
				Assertions.assertThat(context.getRequestorUserName()).isEqualTo("user1");
				Assertions.assertThat(AttributeContexts.peek().getAttribute(UserInfoAttribute.class).roles()).contains("role2");
			}
			return Neutral.NEUTRAL;
		});
	}
}
