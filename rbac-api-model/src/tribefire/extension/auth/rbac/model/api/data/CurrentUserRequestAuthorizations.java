package tribefire.extension.auth.rbac.model.api.data;

import java.util.List;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

public interface CurrentUserRequestAuthorizations extends GenericEntity {

	EntityType<CurrentUserRequestAuthorizations> T = EntityTypes.T(CurrentUserRequestAuthorizations.class);
	
	String authorizations = "authorizations";
	
	List<CurrentUserRequestAuthorization> getAuthorizations();
	void setAuthorizations(List<CurrentUserRequestAuthorization> authorizations);
}

