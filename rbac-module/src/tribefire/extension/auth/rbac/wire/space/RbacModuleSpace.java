
package tribefire.extension.auth.rbac.wire.space;

import com.braintribe.model.deployment.DeploymentStatus;
import com.braintribe.model.extensiondeployment.HardwiredServicePreProcessor;
import com.braintribe.model.extensiondeployment.meta.PreProcessWith;
import com.braintribe.model.generic.reflection.Model;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.deployment.api.binding.DenotationBindingBuilder;
import com.braintribe.model.processing.meta.editor.ModelMetaDataEditor;
import com.braintribe.model.processing.session.api.collaboration.PersistenceInitializationContext;
import com.braintribe.model.processing.session.api.managed.ManagedGmSession;
import com.braintribe.model.processing.session.api.managed.ModelAccessoryFactory;
import com.braintribe.model.service.api.AuthorizedRequest;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

import tribefire.extension.auth._RbacConfiguredApiModel_;
import tribefire.extension.auth.rbac.processing.ServiceAuthorizationPreProcessor;
import tribefire.module.api.InitializerBindingBuilder;
import tribefire.module.api.WireContractBindingBuilder;
import tribefire.module.wire.contract.ModelApiContract;
import tribefire.module.wire.contract.TribefireModuleContract;
import tribefire.module.wire.contract.TribefireWebPlatformContract;
import tribefire.module.wire.contract.WebPlatformHardwiredDeployablesContract;

@Managed
public class RbacModuleSpace implements TribefireModuleContract {

	private static final String GLOBAL_ID_PREPROCESSOR_SERVICE_AUTH = "hardwired:preprocessor/service.auth";

	@Import
	private TribefireWebPlatformContract tfPlatform;
	
	@Import
	private ModelApiContract modelApi;

	//
	// WireContracts
	//

	@Override
	public void bindWireContracts(WireContractBindingBuilder bindings) {
		// Bind wire contracts to make them available for other modules.
		// Note that the Contract class cannot be defined in this module, but must be in a gm-api artifact.
	}

	//
	// Hardwired deployables
	//

	@Override
	public void bindHardwired() {
		WebPlatformHardwiredDeployablesContract hardwiredDeployables = tfPlatform.hardwiredDeployables();
		
		hardwiredDeployables //
		.bind(serviceAuthorizationPreProcessorDeployable()) //
		.component(tfPlatform.binders().servicePreProcessor(), this::serviceAuthorizationPreProcessor);
	}

	//
	// Initializers
	//

	@Override
	public void bindInitializers(InitializerBindingBuilder bindings) {
		bindings.bind(this::initialize);
	}

	//
	// Deployables
	//

	@Override
	public void bindDeployables(DenotationBindingBuilder bindings) {
		// Bind deployment experts for deployable denotation types.
		// Note that the basic component binders (for e.g. serviceProcessor or incrementalAccess) can be found via tfPlatform.deployment().binders(). 
	}

	//
	// Wiring
	//
	
	@Managed
	private HardwiredServicePreProcessor serviceAuthorizationPreProcessorDeployable() {
		HardwiredServicePreProcessor bean = HardwiredServicePreProcessor.T.create();
		bean.setName("Service Authorization PreProcessor");
		bean.setExternalId("preprocessor.service.auth");
		bean.setGlobalId(GLOBAL_ID_PREPROCESSOR_SERVICE_AUTH);
		bean.setAutoDeploy(true);
		bean.setDeploymentStatus(DeploymentStatus.deployed);
		return bean;
	}
	
	@Managed
	private ServiceAuthorizationPreProcessor serviceAuthorizationPreProcessor() {
		ServiceAuthorizationPreProcessor bean = new ServiceAuthorizationPreProcessor();
		ModelAccessoryFactory modelAccessoryFactory = tfPlatform.requestUserRelated().modelAccessoryFactory();
		bean.setMdResolverLookup(domainId -> modelAccessoryFactory.getForServiceDomain(domainId).getCmdResolver());
		return bean;
	}
	
	private void initialize(PersistenceInitializationContext context) {
		ManagedGmSession session = context.getSession();
		
		GmMetaModel apiModel = session.findEntityByGlobalId(Model.modelGlobalId(_RbacConfiguredApiModel_.name));
		ModelMetaDataEditor editor = modelApi.newMetaDataEditor(apiModel).done();

		HardwiredServicePreProcessor processor = session.findEntityByGlobalId(GLOBAL_ID_PREPROCESSOR_SERVICE_AUTH);
		
		PreProcessWith preProcessWith = session.create(PreProcessWith.T);
		preProcessWith.setGlobalId("preProcessWith.service.auth");
		preProcessWith.setProcessor(processor);
		
		editor.onEntityType(AuthorizedRequest.T).addMetaData(preProcessWith);
		
	}
}