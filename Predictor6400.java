public class Predictor6400 extends Predictor {

//==================================== GSHARE - PAP - TOURNAMENT - PREDICTOR =====================================
	// Variable for Gshare----------------------------------------------------------------------------------------
	int n_G;            // n - LSB bits from PC content
	int k_G;		    // k - Content bits of GHR register
	int n1_G; 	    	// n1 - bit from which xor starts
	int sat_G;    	    // no of saturation counter bits  
	Table pht_tab_G;    // PHT table 
	Register ghr_reg_G; // GHR register 

    // Variable for PAP -------------------------------------------------------------------------------------------
	int n_P;            // n - LSB bits from PC content
	int k_P;            // k - Content bits of GHR register
	int n1_P;           // n1 - LSB bits from PC content 
	Table pht_tab_P;    // PHT table
	Table ghr_tab_P;    // GHR table

	// Variable for Tournament ------------------------------------------------------------------------------------
	Table tab_T;
	Register reg_T;

	// Function to extract desired bits from address(PC) ==========================================================
	int get_PC_bits(long bitSeries, int s_bit, int e_bit){	

		int X_bits = e_bit - s_bit + 1;
		long Y=  bitSeries & (((1<<X_bits)-1)<<s_bit);
		long Z = Y>>s_bit;
		return (int)Z;
	}

	// Constructor for Predictor 6400 =============================================================================
	public Predictor6400() {

		//initializing Tournament ---------------------------------------------------------------------------------
        tab_T = new Table(1<<10,2);
		reg_T = new Register(8);

        //initializing Gshare--------------------------------------------------------------------------------------
		n_G = 10;
		k_G = 8;
		n1_G = 2; 
		sat_G = 2;
		pht_tab_G = new Table(1<<n_G,sat_G);
		ghr_reg_G = reg_T;

        //initializing Pap-----------------------------------------------------------------------------------------  
		n_P = 7;
		n1_P = 6;
		k_P = 3;
		pht_tab_P = new Table(1<<(n_P + k_P),2);
		ghr_tab_P = new Table(1<<n1_P,k_P);
    }

	// Trainer function for Gshare predictor=======================================================================
	void G_trainer(long address,boolean outcome){
        
        int ind_pht_tab_G = 0;
        ind_pht_tab_G = (get_PC_bits(address,0,n_G-1))^(reg_T.getInteger(0,k_G-1)<<n1_G);
        reg_T.setInteger(0,k_G-1,(reg_T.getInteger(1,k_G-1)<<1) + (outcome?1:0));

        int sat_val_G = pht_tab_G.getInteger(ind_pht_tab_G,0,1); 
        if (outcome && (sat_val_G==0 || sat_val_G==1 || sat_val_G==2)){
            sat_val_G++;
            pht_tab_G.setInteger(ind_pht_tab_G,0,1,sat_val_G);
        } 	
        else if((!outcome) && (sat_val_G==1 || sat_val_G==2 || sat_val_G==3)){
            sat_val_G--;
            pht_tab_G.setInteger(ind_pht_tab_G,0,1,sat_val_G);
        }
	}

    // Trainer function for PAP predictor =========================================================================
	void P_trainer(long address,boolean outcome){

		int ind_ghr_tab_P = get_PC_bits(address,0, n1_P-1);//index to GHR table
		int val_ghr_reg_P = ghr_tab_P.getInteger(ind_ghr_tab_P,0,k_P-1);
		int ind_pht_tab_P = (get_PC_bits(address,0,n_P-1)<<k_P) + val_ghr_reg_P;
        int sat_val_P = pht_tab_P.getInteger(ind_pht_tab_P,0,1); 
        if (outcome && (sat_val_P==0 || sat_val_P==1 || sat_val_P==2)){
            sat_val_P++;
            pht_tab_P.setInteger(ind_pht_tab_P,0,1,sat_val_P);
        } 	
        else if((!outcome) && (sat_val_P==1 || sat_val_P==2 || sat_val_P==3)){
            sat_val_P--;
            pht_tab_P.setInteger(ind_pht_tab_P,0,1,sat_val_P);
        }               
        ghr_tab_P.setInteger(ind_ghr_tab_P,0,k_P-1,(ghr_tab_P.getInteger(ind_ghr_tab_P,1,k_P-1)*2)+(outcome?1:0));
	}

    // Trainer function for Tournament predictor ===================================================================
	void T_trainer(long address,boolean outcome,boolean predict)
	{
		int ind_tab_T = get_PC_bits(address,0,9);//n_G-1);
		ind_tab_T = (get_PC_bits(address,0,9)) ^ ((ghr_reg_G.getInteger(0,7)<<2));
		
        if (P_predictor(address)!=G_predictor(address)) {
            
            int sat_val_T = tab_T.getInteger(ind_tab_T,0,1); 
            if ((outcome == G_predictor(address)) && (sat_val_T==0 || sat_val_T==1 || sat_val_T==2)){
                sat_val_T++;
                tab_T.setInteger(ind_tab_T,0,1,sat_val_T);
            } 	
            else if((outcome != G_predictor(address)) && (sat_val_T==1 || sat_val_T==2 || sat_val_T==3)){
                sat_val_T--;
                tab_T.setInteger(ind_tab_T,0,1,sat_val_T);
            } 
		}
				
		G_trainer(address,outcome);
		P_trainer(address,outcome);
	}

//=================================================================================================================
	// Predictor function for Gshare predictor --------------------------------------------------------------------
	boolean G_predictor(long address){
		int ind_pht_tab_G = (get_PC_bits(address,0,n_G-1)) ^ ((ghr_reg_G.getInteger(0,k_G-1)<<n1_G));
		return (pht_tab_G.getInteger(ind_pht_tab_G,0,1)>1) ? true : false;
	}
	
	// Predictor function for PAP predictor -----------------------------------------------------------------------
	boolean P_predictor(long address){
		int ind_pht_tab_P = get_PC_bits(address,0,n1_P-1);
		int temp = ghr_tab_P.getInteger(ind_pht_tab_P,0,k_P-1);
		ind_pht_tab_P = ((get_PC_bits(address,0,n_P-1))<<k_P) + temp;
		return (pht_tab_P.getInteger(ind_pht_tab_P,0,1)>1) ? true : false;
	}

	// Predictor function for Tournament predictor -----------------------------------------------------------------
	boolean T_predictor(long address){
		int ind_tab_T = get_PC_bits(address,0,9);//n_G-1);
		ind_tab_T = (get_PC_bits(address,0,9)) ^ ((ghr_reg_G.getInteger(0,7)<<2));
		return (tab_T.getInteger(ind_tab_T,0,1) >1) ? G_predictor(address) : P_predictor(address);
    }

//=================================================================================================================
// Function to call predictors training ---------------------------------------------------------------------------	
public void Train(long address, boolean outcome, boolean predict) {
		T_trainer(address,outcome,predict);
	}

// Function to return prediction result ----------------------------------------------------------------------------	
	public boolean predict(long address){
		return T_predictor(address);
	}
}
//==================================================== CODE END ====================================================
