package tribefire.extension.auth.rbac.model.api.data;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

public interface CurrentUserRequestAuthorization extends RequestAuthorization {
	EntityType<CurrentUserRequestAuthorization> T = EntityTypes.T(CurrentUserRequestAuthorization.class);
	
	String allowed = "allowed";
	
	boolean getAllowed();
	void setAllowed(boolean allowed);
}

