package de.flexiprovider.my;


class SignedMutableBigInteger extends MutableBigInteger {

    int sign = 1;

    SignedMutableBigInteger() {
        super();
    }


    SignedMutableBigInteger(int val) {
        super(val);
    }

    void signedAdd(SignedMutableBigInteger addend) {
        if (sign == addend.sign)
            add(addend);
        else
            sign = sign * subtract(addend);

    }


    void signedAdd(MutableBigInteger addend) {
        if (sign == 1)
            add(addend);
        else
            sign = sign * subtract(addend);

    }


    void signedSubtract(SignedMutableBigInteger addend) {
        if (sign == addend.sign)
            sign = sign * subtract(addend);
        else
            add(addend);

    }


    public String toString() {
        return this.toBigInteger(sign).toString();
    }

}
