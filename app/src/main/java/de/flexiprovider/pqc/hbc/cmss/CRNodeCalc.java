package de.flexiprovider.pqc.hbc.cmss;

import de.flexiprovider.api.MessageDigest;

/**
 * This class provides an implementation of the {@link de.flexiprovider.pqc.hbc.cmss.NodeCalc} interface, that
 * calculates the nodes in the classical way without use of SPR.
 * 
 */
public class CRNodeCalc implements NodeCalc {
    private MessageDigest md;

    /**
     * Constructs a new {@link de.flexiprovider.pqc.hbc.cmss.CRNodeCalc} object, that uses the given
     * {@link de.flexiprovider.api.MessageDigest} to calculate the nodes.
     * 
     * @param md
     *                the {@link de.flexiprovider.api.MessageDigest} to use
     */
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
