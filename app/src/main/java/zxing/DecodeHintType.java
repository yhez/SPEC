package zxing;

public enum DecodeHintType {

 OTHER(),

  PURE_BARCODE(),

  POSSIBLE_FORMATS(),

  TRY_HARDER(),

  CHARACTER_SET(),

  ALLOWED_LENGTHS(),

  ASSUME_CODE_39_CHECK_DIGIT(),

  ASSUME_GS1(),

  RETURN_CODABAR_START_END(),

  NEED_RESULT_POINT_CALLBACK(),

  ;

    DecodeHintType() {
    }

}
