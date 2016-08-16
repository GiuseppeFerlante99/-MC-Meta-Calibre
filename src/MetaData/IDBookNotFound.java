/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package MetaData;

/**
 *
 * @author Giuseppe
 */
public class IDBookNotFound extends Exception{
    public IDBookNotFound(String nameBook){
        super("ID libro "+nameBook+" non trovato");
    }
}
