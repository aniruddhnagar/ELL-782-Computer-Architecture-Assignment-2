public class Predictor9999 extends Predictor {

//====================================== GSHARE + LOCAL - BRANCH - PREDICTOR =====================================
	// Variable for Select----------------------------------------------------------------------------------------
	Table tab_Select;
	Register pc;

	// Variable for Gshare----------------------------------------------------------------------------------------
	Table pht_tab_G;
	Register ghr_reg_G;
	
	// Variable for Local-----------------------------------------------------------------------------------------
	Table lht_tab_LH;
	Table pht_tab_LH;

	// Constructor for Predictor 9999 ============================================================================	
	public Predictor9999() {

		//initializing Select table ------------------------------------------------------------------------------		
		tab_Select = new Table(1<<10,2);
		pc = new Register(32);	
	
		//initializing Gshare-------------------------------------------------------------------------------------
		pht_tab_G = new Table(1<<10,2);
		ghr_reg_G = new Register(10);
		
		//initializing Local -------------------------------------------------------------------------------------	
		lht_tab_LH = new Table(1<<9,9);
		pht_tab_LH = new Table(1<<9,2);
	}
		
	// Function to train predictor ================================================================================
	public boolean L_trainer(long address, boolean outcome, boolean predict){

		// Reading sat values from Local --------------------------------------------------------------------------
		pc.setInteger(0,31,(int)address);
		int ind_lht_tab_LH = pc.getInteger(21,29);
		int ind_pht_tab_LH = lht_tab_LH.getInteger(ind_lht_tab_LH,0,8);
		int sat_val_LH = pht_tab_LH.getInteger(ind_pht_tab_LH,0,1);
		boolean LH_predict = (sat_val_LH <= 1 ? false : true);

		// Training Local Predictor --------------------------------------------------------------------------------
		if(outcome && (sat_val_LH == 1 || sat_val_LH == 2))
			pht_tab_LH.setInteger(ind_pht_tab_LH,0,1,3);
		else if(outcome && (sat_val_LH == 0))
			pht_tab_LH.setInteger(ind_pht_tab_LH,0,1,1);
		else if((!outcome) && (sat_val_LH == 1 || sat_val_LH == 2))
			pht_tab_LH.setInteger(ind_pht_tab_LH,0,1,0);
		else if((!outcome) && (sat_val_LH == 3))
			pht_tab_LH.setInteger(ind_pht_tab_LH,0,1,2);

		ind_pht_tab_LH = (ind_pht_tab_LH << 1) + (outcome ? 1 : 0);
		lht_tab_LH.setInteger(ind_lht_tab_LH,0,8,ind_pht_tab_LH);
		return LH_predict;
	}
		
	// Function to train predictor ================================================================================
	public boolean G_trainer(long address, boolean outcome, boolean predict){

		// Reading sat values from Gshare ------------------------------------------------------------------------
		pc.setInteger(0,31,(int)address);
		int val_ghr_reg_G = ghr_reg_G.getInteger(0,9);
		int	ind_pht_tab_G = (pc.getInteger(20,29)) ^ val_ghr_reg_G;
		int sat_val_G = pht_tab_G.getInteger(ind_pht_tab_G,0,1);
		boolean G_predict = (sat_val_G <= 1 ? false : true);
	
		// Training Gshare Predictor-------------------------------------------------------------------------------
		if(outcome && (sat_val_G == 1 || sat_val_G == 2))
			pht_tab_G.setInteger(ind_pht_tab_G,0,1,3);
		else if(outcome && (sat_val_G == 0))
			pht_tab_G.setInteger(ind_pht_tab_G,0,1,1);
		else if((!outcome) && (sat_val_G == 1 || sat_val_G == 2))
			pht_tab_G.setInteger(ind_pht_tab_G,0,1,0);
		else if((!outcome) && (sat_val_G == 3))
			pht_tab_G.setInteger(ind_pht_tab_G,0,1,2);

		val_ghr_reg_G = (val_ghr_reg_G << 1) + (outcome ? 1 : 0);
		ghr_reg_G.setInteger(0,9,val_ghr_reg_G);
		return G_predict;
	}

	// Function to train predictor ================================================================================
	public void Train(long address, boolean outcome, boolean predict) {
		
		boolean LH_predict = L_trainer(address, outcome, predict);
		boolean G_predict = G_trainer(address, outcome, predict);
	
		// Reading sat values from select table ------------------------------------------------------------------
		int ind_tab_select = pc.getInteger(20,29);
		int val_tab_select = tab_Select.getInteger(ind_tab_select,0,1);

		// Training Table Selector --------------------------------------------------------------------------------
		if((G_predict != LH_predict)&&(G_predict == outcome)){
			if(val_tab_select == 1 || val_tab_select == 2)
				tab_Select.setInteger(ind_tab_select,0,1,0);
			else if(val_tab_select == 3)
				tab_Select.setInteger(ind_tab_select,0,1,2);
			}
		else if((G_predict != LH_predict)&&(G_predict != outcome)){
			if(val_tab_select == 1 || val_tab_select == 2)
				tab_Select.setInteger(ind_tab_select,0,1,3);
			else if(val_tab_select == 0)
				tab_Select.setInteger(ind_tab_select,0,1,1);
		}	
	}
			
//==================================================================================================================
	//Function to return prediction result -------------------------------------------------------------------------	
	public boolean predict(long address){

		pc.setInteger(0,31,(int)address);
		// select table---------------------------------------------------------------------------------------------
		int ind_tab_select = pc.getInteger(20,29);
		int val_tab_select = tab_Select.getInteger(ind_tab_select,0,1);
		
		// Gshare --------------------------------------------------------------------------------------------------
		int val_ghr_reg_G = ghr_reg_G.getInteger(0,9);
		int ind_pht_tab_G = pc.getInteger(20,29);
		ind_pht_tab_G = ind_pht_tab_G ^ val_ghr_reg_G;
		int sat_val_G = pht_tab_G.getInteger(ind_pht_tab_G,0,1);
		
		// Local Predictor -----------------------------------------------------------------------------------------
		int ind_lht_tab_LH = pc.getInteger(21,29);
		int ind_pht_tab_LH = lht_tab_LH.getInteger(ind_lht_tab_LH,0,8);
		int sat_val_LH = pht_tab_LH.getInteger(ind_pht_tab_LH,0,1);
		
		return (val_tab_select <= 1) ? ((sat_val_G <= 1) ? false:true) : ((sat_val_LH <= 1) ? false:true);
	}
}
//================================================= CODE END ======================================================