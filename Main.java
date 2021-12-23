

import java.io.BufferedReader;
import java.io.FileReader;

public class Main {

	static int size=0;
	public static void main(String[] args)throws Exception {

		BufferedReader br = new BufferedReader(new FileReader(args[0]));

		Predictor branchPredictor = null;
		
		if(Integer.parseInt(args[1]) == 2400) {
			branchPredictor = new Predictor2400();
		} else if(Integer.parseInt(args[1]) == 6400) {
			branchPredictor = new Predictor6400();
		} else if(Integer.parseInt(args[1]) == 9999) {
			branchPredictor = new Predictor9999();
		}else if(Integer.parseInt(args[1]) == 32000) {
			branchPredictor = new Predictor32000();
		}
		
		try {
			String line = br.readLine();
			long numBranches=0;
			long correctPred=0;
			while (line != null) {
				numBranches++;
				//System.out.println(line);
				String tokens[]=line.split(" ");
				if(tokens[0].startsWith(".")){
					tokens[0]=tokens[0].substring(1);
				}
				Long pc=Long.parseLong(tokens[0]);
				int bt=Integer.parseInt(tokens[1]);

				boolean branchTaken=(bt==1)?true:false;

				//call predict
				boolean predict=branchPredictor.predict(pc);
				if(predict==branchTaken){
					correctPred++;
				}
				//call train
				branchPredictor.Train(pc, branchTaken, predict);
				line = br.readLine();
			}
			System.out.println("Size of predictor: "+size);
			System.out.println("Total branches: "+numBranches);
			double accuracy=(double)correctPred/numBranches;
			System.out.println("Predictor Accuracy: "+accuracy);

		} finally {
			br.close();
		}
	}

}