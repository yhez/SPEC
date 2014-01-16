package de.flexiprovider.common.exceptions;


public class InvalidFormatException extends ECException {


    public InvalidFormatException(byte type) {
        super("diagnostic:\n" + type + " is an invalid type of point"
                + "representation:\n\t2, 3: compressed form,\n\t4"
                + ": uncompressed form\n\t6, 7: hybrid form");
    }

}
