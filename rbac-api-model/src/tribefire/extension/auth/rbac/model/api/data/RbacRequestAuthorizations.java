package tribefire.extension.auth.rbac.model.api.data;

import java.util.List;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

public interface RbacRequestAuthorizations extends GenericEntity {
	EntityType<RbacRequestAuthorizations> T = EntityTypes.T(RbacRequestAuthorizations.class);
	
	String authorizations = "authorizations";
	
	List<RbacRequestAuthorization> getAuthorizations();
	void setAuthorizations(List<RbacRequestAuthorization> authorizations);
}

