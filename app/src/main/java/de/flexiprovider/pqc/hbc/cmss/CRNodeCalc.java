package de.flexiprovider.pqc.hbc.cmss;

import de.flexiprovider.api.MessageDigest;

/**
 * This class provides an implementation of the {@link NodeCalc} interface, that
 * calculates the nodes in the classical way without use of SPR.
 */
public class CRNodeCalc implements NodeCalc {
    private MessageDigest md;


    public CRNodeCalc(MessageDigest md) {
        this.md = md;
    }

    public byte[] computeParent(byte[] leftNode, byte[] rightNode, int height) {
        md.update(leftNode);
        md.update(rightNode);
        return md.digest();
    }

    public byte[] getLeaf(byte[] vkey) {
        return md.digest(vkey);
    }

}
