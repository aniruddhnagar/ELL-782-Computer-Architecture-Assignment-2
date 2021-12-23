public class Predictor2400 extends Predictor {

//================================================ GSHARE PREDICTOR ============================================
	// Variable for Gshare--------------------------------------------------------------------------------------
	int n_G;            // n - LSB bits from PC content
	int k_G;		    // k - Content bits of GHR register
	int n1_G; 	    	// n1 - no of left shift required by GHR to xor with n
	int sat_G;    	    // no of saturation counter bits  
	Table pht_tab_G;    // PHT table 
	Register ghr_reg_G; // GHR register 

	// Function to extract desired bits from address(PC) =======================================================
    int get_PC_bits(long bitSeries, int s_bit, int e_bit){	

		int X_bits = e_bit - s_bit + 1;
    	long Y=  bitSeries & (((1<<X_bits)-1)<<s_bit);
	    long Z = Y>>s_bit;
	    return (int)Z;
	}

	// Constructor for Predictor 2400 ==========================================================================
    public Predictor2400() {

		n_G = 10;
		k_G = 8;
		n1_G = 2; 
		sat_G = 2;
        pht_tab_G = new Table((1<<n_G),sat_G);
		ghr_reg_G = new Register(k_G);
	}

	// Function to train predictor result ======================================================================
	public void Train(long address, boolean outcome, boolean predict) {

		// Xor of n_G and K_G bits--- 
		int ind_pht = get_PC_bits(address,0,n_G-1) ^ (ghr_reg_G.getInteger(0,k_G-1)<<n1_G);
		// seting GHR register value---
		ghr_reg_G.setInteger(0,k_G-1,(ghr_reg_G.getInteger(1,k_G-1)<<1)+(outcome?1:0));
		// geting saturation counter value---
		int sat_val_G = pht_tab_G.getInteger(ind_pht,0,1);
		// seting saturation counter value---
        if (outcome && (sat_val_G==0 || sat_val_G==1 || sat_val_G==2)){
            sat_val_G++;
            pht_tab_G.setInteger(ind_pht,0,1,sat_val_G);
        } 	
        else if((!outcome) && (sat_val_G==1 || sat_val_G==2 || sat_val_G==3)){
            sat_val_G--;
            pht_tab_G.setInteger(ind_pht,0,1,sat_val_G);
        }
	}

	// Function to return prediction result ====================================================================
	public boolean predict(long address){

		int ind_pht = get_PC_bits(address,0,n_G-1) ^ (ghr_reg_G.getInteger(0,k_G-1)<<n1_G);
		int sat_val_G = pht_tab_G.getInteger(ind_pht,0,1);
        return (sat_val_G>1)?true:false;
	}
}
//================================================= CODE END ===================================================
