/* ========================================================================
 *
 *  This file is part of CODEC, which is a Java package for encoding
 *  and decoding ASN.1 data structures.
 *
 *  Author: Fraunhofer Institute for Computer Graphics Research IGD
 *          Department A8: Security Technology
 *          Fraunhoferstr. 5, 64283 Darmstadt, Germany
 *
 *  Rights: Copyright (c) 2004 by Fraunhofer-Gesellschaft 
 *          zur Foerderung der angewandten Forschung e.V.
 *          Hansastr. 27c, 80686 Munich, Germany.
 *
 * ------------------------------------------------------------------------
 *
 *  The software package is free software; you can redistribute it and/or 
 *  modify it under the terms of the GNU Lesser General Public License as 
 *  published by the Free Software Foundation; either version 2.1 of the 
 *  License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful, but 
 *  WITHOUT ANY WARRANTY; without even the implied warranty of 
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public 
 *  License along with this software package; if not, write to the Free 
 *  Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, 
 *  MA 02110-1301, USA or obtain a copy of the license at 
 *  http://www.fsf.org/licensing/licenses/lgpl.txt.
 *
 * ------------------------------------------------------------------------
 *
 *  The CODEC library can solely be used and distributed according to 
 *  the terms and conditions of the GNU Lesser General Public License for 
 *  non-commercial research purposes and shall not be embedded in any 
 *  products or services of any user or of any third party and shall not 
 *  be linked with any products or services of any user or of any third 
 *  party that will be commercially exploited.
 *
 *  The CODEC library has not been tested for the use or application 
 *  for a determined purpose. It is a developing version that can 
 *  possibly contain errors. Therefore, Fraunhofer-Gesellschaft zur 
 *  Foerderung der angewandten Forschung e.V. does not warrant that the 
 *  operation of the CODEC library will be uninterrupted or error-free. 
 *  Neither does Fraunhofer-Gesellschaft zur Foerderung der angewandten 
 *  Forschung e.V. warrant that the CODEC library will operate and 
 *  interact in an uninterrupted or error-free way together with the 
 *  computer program libraries of third parties which the CODEC library 
 *  accesses and which are distributed together with the CODEC library.
 *
 *  Fraunhofer-Gesellschaft zur Foerderung der angewandten Forschung e.V. 
 *  does not warrant that the operation of the third parties's computer 
 *  program libraries themselves which the CODEC library accesses will 
 *  be uninterrupted or error-free.
 *
 *  Fraunhofer-Gesellschaft zur Foerderung der angewandten Forschung e.V. 
 *  shall not be liable for any errors or direct, indirect, special, 
 *  incidental or consequential damages, including lost profits resulting 
 *  from the combination of the CODEC library with software of any user 
 *  or of any third party or resulting from the implementation of the 
 *  CODEC library in any products, systems or services of any user or 
 *  of any third party.
 *
 *  Fraunhofer-Gesellschaft zur Foerderung der angewandten Forschung e.V. 
 *  does not provide any warranty nor any liability that utilization of 
 *  the CODEC library will not interfere with third party intellectual 
 *  property rights or with any other protected third party rights or will 
 *  cause damage to third parties. Fraunhofer Gesellschaft zur Foerderung 
 *  der angewandten Forschung e.V. is currently not aware of any such 
 *  rights.
 *
 *  The CODEC library is supplied without any accompanying services.
 *
 * ========================================================================
 */
package codec.x509;

import codec.asn1.ASN1Choice;
import codec.asn1.ASN1IA5String;
import codec.asn1.ASN1ObjectIdentifier;
import codec.asn1.ASN1OctetString;
import codec.asn1.ASN1OpenType;
import codec.asn1.ASN1Sequence;
import codec.asn1.ASN1TaggedType;
import codec.asn1.ASN1Type;
import codec.x501.Name;

/**
 * This class represents the <em>GeneralName</em> data type as denoted in
 * X.509. It implements the following ASN1 data structure:
 * <p/>
 * <p/>
 * <pre>
 * GeneralName ::= CHOICE {
 *    otherName                     [0]  IMPLICIT OtherName
 *    rfc822Name                    [1]  IMPLICIT IA5String
 *    dNSName                       [2]  IMPLICIT IA5String
 *    x400Address                   [3]  IMPLICIT ORAAddress
 *    directoryName                 [4]  IMPLICIT Name
 *    ediPartyName                  [5]  IMPLICIT EDIPartyName
 *    uniformRessourceIdentifier    [6]  IMPLICIT IA5String
 *    iPAddress                     [7]  IMPLICIT OCTET STRING
 *    registeredID                  [8]  IMPLICIT OBJECT IDENTIFIER
 * }
 * OtherName ::= SEQUENCE {
 *    type-id    OBJECT IDENTIFIER,
 *    value      [0] EXPLICIT ANY DEFINED BY type-id
 * }
 * </pre>
 * <p/>
 * Note that x400Address and ediPartyName are not yet implemented and will cause
 * exceptions to be thrown.
 *
 * @author Markus Tak
 */
public class GeneralName extends ASN1Choice {

    /**
     * This value indicates the choice "otherName"
     */
    public static final int TYPE_OTHER_NAME = 0;
    private ASN1TaggedType otherName_;
    private ASN1Sequence otherNameSequence_;
    private ASN1ObjectIdentifier otherNameID_;
    private ASN1TaggedType otherNameValueTag_;
    private ASN1OpenType otherNameValue_;

    /**
     * This value indicates the choice "rfc822Name"
     */
    public static final int TYPE_RFC822_NAME = 1;
    private ASN1IA5String rfc822N_;
    private ASN1TaggedType rfc822Name_;

    /**
     * This value indicates the choice "dNSName"
     */
    public static final int TYPE_DNS_NAME = 2;
    private ASN1IA5String dNSN_;
    private ASN1TaggedType dNSName_;

    /**
     * This value indicates the choice "x400Address"
     */
    public static final int TYPE_X400_ADDRESS = 3;

    /**
     * This value indicates the choice "directoryName"
     */
    public static final int TYPE_DIRECTORY_NAME = 4;
    private Name dirN_;
    private ASN1TaggedType directoryName_;

    /**
     * This value indicates the choice "ediPartyName"
     */
    public static final int TYPE_EDI_PARTY_NAME = 5;

    /**
     * This value indicates the choice "uniformResourceIdentifier"
     */
    public static final int TYPE_UNIFORM_RESOURCE_IDENTIFIER = 6;
    private ASN1IA5String uniformResourceId_;
    private ASN1TaggedType uniformResourceIdentifier_;

    /**
     * This value indicates the choice "iPAddress"
     */
    public static final int TYPE_IP_ADDRESS = 7;
    private ASN1OctetString iPAdr_;
    private ASN1TaggedType iPAddress_;

    /**
     * This value indicates the choice "registeredID"
     */
    public static final int TYPE_REGISTERED_ID = 8;
    private ASN1ObjectIdentifier regID_;
    private ASN1TaggedType registeredID_;

    /**
     * Builds the structure of the class ready for decoding
     */
    public GeneralName() {
        super();

        otherNameID_ = new ASN1ObjectIdentifier();
        otherNameValue_ = new ASN1OpenType();

        otherNameValueTag_ = new ASN1TaggedType(0, otherNameValue_, true, false);

        otherNameSequence_ = new ASN1Sequence();
        otherNameSequence_.add(otherNameID_);
        otherNameSequence_.add(otherNameValueTag_);

        otherName_ = new ASN1TaggedType(TYPE_OTHER_NAME, otherNameSequence_,
                false, false);
        addType(otherName_);

        rfc822N_ = new ASN1IA5String();
        rfc822Name_ = new ASN1TaggedType(TYPE_RFC822_NAME, rfc822N_, false,
                false);
        addType(rfc822Name_);

        dNSN_ = new ASN1IA5String();
        dNSName_ = new ASN1TaggedType(TYPE_DNS_NAME, dNSN_, false, false);
        addType(dNSName_);

        // x400Address is not supported yet...

        dirN_ = new Name();
        // directoryName *MUST* be marked explicit here for the decoding
        // operation
        // to succeed. see also the setDirectoryName() method for more
        // information
        // --volker roth & ralf weinmann
        directoryName_ = new ASN1TaggedType(TYPE_DIRECTORY_NAME, dirN_, true,
                false);
        addType(directoryName_);

        // ediPartyName is not supported yet...

        uniformResourceId_ = new ASN1IA5String();
        uniformResourceIdentifier_ = new ASN1TaggedType(
                TYPE_UNIFORM_RESOURCE_IDENTIFIER, uniformResourceId_, false,
                false);
        addType(uniformResourceIdentifier_);

        iPAdr_ = new ASN1OctetString();

        iPAddress_ = new ASN1TaggedType(TYPE_IP_ADDRESS, iPAdr_, false, false);
        addType(iPAddress_);

        regID_ = new ASN1ObjectIdentifier();

        registeredID_ = new ASN1TaggedType(TYPE_REGISTERED_ID, regID_, false,
                false);
        addType(registeredID_);
    }

    /**
     * Returns the actual Name as ASN1Type. In fact, this can be:<br>
     * <li>ASN1IA5String in case of rfcName, dNSName and
     * uniformResourceIdentifier
     * <li>ASN1OctetString in case of iPAddress
     * <li>Name in case of directoryName
     * <li>ASN1ObjectIdentifier in case of registeredID
     * <li>ASN1Sequence in case of otherName
     *
     * @return the actual GeneralName value. The type depends on the chosen
     * representation and can be either ASN1IA5String, ASN1OctetString,
     * Name, ASN1Sequence or ASN1ObjectIdentifier.
     * @throws X509Exception if there was a bad tag
     */
    public ASN1Type getGeneralName() throws X509Exception {
        int tag = getTag();

        // extract the TaggedType first, then the "real" inner
        // value
        ASN1TaggedType inner = (ASN1TaggedType) getInnerType();

        switch (tag) {
            case TYPE_OTHER_NAME:
                return inner.getInnerType();
            case TYPE_RFC822_NAME:
                return inner.getInnerType();
            case TYPE_DNS_NAME:
                return inner.getInnerType();
            case TYPE_X400_ADDRESS:
                throw new X509Exception("x400Address not yet supported!");
            case TYPE_DIRECTORY_NAME:
                return inner.getInnerType();
            case TYPE_EDI_PARTY_NAME:
                throw new X509Exception("ediPartyName not yet supported!");
            case TYPE_UNIFORM_RESOURCE_IDENTIFIER:
                return inner.getInnerType();
            case TYPE_IP_ADDRESS:
                return inner.getInnerType();
            case TYPE_REGISTERED_ID:
                return inner.getInnerType();
            default:
                throw new X509Exception("Tag not supported for GeneralName: " + tag);
        }
    }

    /**
     * Set the GeneralName as uniformResourceIdentifier.Note that the
     * ASN1IA5String object is cloned so that no side effects can occur!
     *
     * @param unirid the name to be set
     */
    public void setUniformResourceIdentifier(ASN1IA5String unirid) {
        uniformResourceId_ = new ASN1IA5String(new String(unirid.getString()));
        uniformResourceId_.setExplicit(false);
        uniformResourceIdentifier_.setInnerType(uniformResourceId_);
        setInnerType(uniformResourceIdentifier_);
    }

    /**
     * This method returns a "human-readable" string representation of this
     * object's state
     *
     * @return a string representation of this object
     */
    public String toString() {

        StringBuffer res = new StringBuffer("GeneralName {\n");

        try {
            res.append(getGeneralName().toString());
        } catch (Exception e) {
            res.append("Caught Exception " + e.getMessage());
            e.printStackTrace();
        }
        res.append("\n}");

        return res.toString();
    }
}
