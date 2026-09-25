package tribefire.extension.auth.rbac_openapi.processing;

import java.util.Set;
import java.util.function.Consumer;

import com.braintribe.cfg.Required;
import com.braintribe.model.processing.meta.cmd.builders.EntityMdResolver;
import com.braintribe.model.processing.meta.cmd.builders.ModelMdResolver;

import tribefire.extension.auth.rbac.processing.ServiceAuthorization;
import tribefire.extension.auth.rbac.processing.ServiceRequestAuthorizationResolver;
import tribefire.extension.webapi.openapi_v3.api.OpenapiDescriptionResolver;

public class RbacRxOpenapiDescriptionResolver implements OpenapiDescriptionResolver {
	private ServiceRequestAuthorizationResolver authorizationResolver;

	@Required
	public void setAuthorizationResolver(ServiceRequestAuthorizationResolver authorizationResolver) {
		this.authorizationResolver = authorizationResolver;
	}

	@Override
	public void resolveEntityDescription(ModelMdResolver modelMdResolver, EntityMdResolver entityMdResolver, Consumer<String> consumer) {
		ServiceAuthorization authorization = authorizationResolver.resolve(entityMdResolver);
		if (!authorization.isPriviledged())
			return;

		consumer.accept("\n\n***");
		consumer.accept("\n\n<b>`Authorization`</b>");
		appendAccessInfo("Allowed Roles", authorization.allowRoles(), consumer);
		appendAccessInfo("Denied Roles", authorization.denyRoles(), consumer);
	}

	private void appendAccessInfo(String context, Set<String> roles, Consumer<String> consumer) {
		if (roles.isEmpty())
			return;

		consumer.accept("\n\n<br/><b>`");
		consumer.accept(context);
		consumer.accept(":`</b>");
		for (String role : roles) {
			consumer.accept(" ");
			consumer.accept(encodeHtml(role));
		}
	}

	private static String encodeHtml(String text) {
		return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;");
	}
}
