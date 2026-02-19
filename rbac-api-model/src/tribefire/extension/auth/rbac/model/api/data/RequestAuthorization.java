package tribefire.extension.auth.rbac.model.api.data;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

public interface RequestAuthorization extends GenericEntity {
	EntityType<RequestAuthorization> T = EntityTypes.T(RequestAuthorization.class);
	
	String type = "type";
	
	String getType();
	void setType(String type);
}

