package MetaData;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
*/

public class Metadata {

    /**
     * @param args the command line arguments
     */
	
    public static void main(String[] args) throws Exception {

        MetaDataLibrary oggetto = new MetaDataLibrary("il seggio vacante", "rowling");
        processIO oggettoIO ;
        String[] meta = oggetto.requestMetaData();

       // String[] meta = new String[]{"il seggio vacante","deescrizione", "rowling","ISBN-10","ISBN-13","true"};
        oggettoIO = new processIO(meta);
        oggettoIO.updateAll();
        

    }
    
    
}
