package tribefire.extension.auth.rbac_openapi.processing;

import java.util.Set;
import java.util.function.Consumer;

import com.braintribe.cfg.Required;
import com.braintribe.model.processing.meta.cmd.builders.EntityMdResolver;
import com.braintribe.model.processing.meta.cmd.builders.ModelMdResolver;
import com.braintribe.utils.DOMTools;

import tribefire.extension.auth.rbac.processing.ServiceAuthorization;
import tribefire.extension.auth.rbac.processing.ServiceRequestAuthorizationResolver;
import tribefire.extension.webapi.openapi_v3.api.OpenapiDescriptionResolver;

public class RbacOpenapiDescriptionResolver implements OpenapiDescriptionResolver {
	private ServiceRequestAuthorizationResolver authorizationResolver;
	
	@Required
	public void setAuthorizationResolver(ServiceRequestAuthorizationResolver authorizationResolver) {
		this.authorizationResolver = authorizationResolver;
	}
	
	@Override
	public void resolveEntityDescription(ModelMdResolver modelMdResolver, EntityMdResolver entityMdResolver, Consumer<String> consumer) {
		ServiceAuthorization serviceAuthorization = authorizationResolver.resolve(entityMdResolver);
		if (serviceAuthorization.isPriviledged()) {
			consumer.accept("\n\n***");
			consumer.accept("\n\n<b>`Authorization`</b>");
			Set<String> allowRoles = serviceAuthorization.allowRoles();
			Set<String> denyRoles = serviceAuthorization.denyRoles();
			
			appendAccessInfo("Allowed Roles", allowRoles, consumer);
			appendAccessInfo("Denied Roles", denyRoles, consumer);
		}
	}
	
	private void appendAccessInfo(String context, Set<String> roles, Consumer<String> consumer) {
		if (roles.isEmpty())
			return;
		
		consumer.accept("\n\n<br/>");
		consumer.accept("<b>`");
		consumer.accept(context);
		consumer.accept(":`</b>");
		
		for (String role: roles) {
			consumer.accept(" ");
			consumer.accept(DOMTools.encode(role));
		}
	}
}
