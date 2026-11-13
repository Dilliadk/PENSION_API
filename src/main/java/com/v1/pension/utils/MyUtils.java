package com.v1.pension.utils;

import java.util.List;
import java.util.Objects;

public class MyUtils {
  public static String amountInNepaliWord(Double amount) {
    if (amount == null)
      amount = 0.0;
    String number = String.format("%.2f", amount);

    if (number.contains(".")) {
      var pos = number.indexOf(".");
      String full = number.substring(0, pos);
      String paisa = number.substring(pos + 1);
      paisa = String.format("%02d", Integer.parseInt(paisa));
      if (!List.of("0", "00").contains(paisa))
        return numberToWordConvertor(full) + " रुपैया " + numberToWordConvertor(paisa) + " पैसा  मात्र";

      number = full;
    }

    return numberToWordConvertor(number) + " रुपैया मात्र";
  }

  private static String numberToWordConvertor(String number) {
    var length = number.length();
    return switch (length) {
      case 1, 2 -> numberBelow100Convertor(number);
      // 123
      case 3 -> numberBelow100Convertor(number.substring(0, 1)) + " सय " +
          numberBelow100Convertor(number.substring(1));

      // 1,234
      case 4 -> numberBelow100Convertor(number.substring(0, 1)) + " हजार " +
          handleZero(number.substring(1, 2), "सय") +
          numberBelow100Convertor(number.substring(2));

      // 12,345
      case 5 -> numberBelow100Convertor(number.substring(0, 2)) + " हजार " +
          handleZero(number.substring(2, 3), "सय") +
          numberBelow100Convertor(number.substring(3));

      // 1,23,456
      case 6 -> numberBelow100Convertor(number.substring(0, 1)) + " लाख " +
          handleZero(number.substring(1, 3), "हजार") +
          handleZero(number.substring(3, 4), "सय") +
          numberBelow100Convertor(number.substring(4));

      // 12,34,567
      case 7 -> numberBelow100Convertor(number.substring(0, 2)) + " लाख " +
          handleZero(number.substring(2, 4), "हजार") +
          handleZero(number.substring(4, 5), "सय") +
          numberBelow100Convertor(number.substring(5));

      // 1,23,45,678
      case 8 -> numberBelow100Convertor(number.substring(0, 1)) + " करोड " +
          handleZero(number.substring(1, 3), "लाख") +
          handleZero(number.substring(3, 5), "हजार") +
          handleZero(number.substring(5, 6), "सय") +
          numberBelow100Convertor(number.substring(6));

      // 12,34,56,789
      case 9 -> numberBelow100Convertor(number.substring(0, 2)) + " करोड " +
          handleZero(number.substring(2, 4), "लाख") +
          handleZero(number.substring(4, 6), "हजार") +
          handleZero(number.substring(6, 7), "सय") +
          numberBelow100Convertor(number.substring(7));

      // 1,23,45,67,890
      case 10 -> numberBelow100Convertor(number.substring(0, 1)) + " अर्ब " +
          handleZero(number.substring(1, 3), "करोड") +
          handleZero(number.substring(3, 5), "लाख") +
          handleZero(number.substring(5, 7), "हजार") +
          handleZero(number.substring(7, 8), "सय") +
          numberBelow100Convertor(number.substring(8));

      // 12,34,56,78,900
      case 11 -> numberBelow100Convertor(number.substring(0, 2)) + " अर्ब " +
          handleZero(number.substring(2, 4), "करोड") +
          handleZero(number.substring(4, 6), "लाख") +
          handleZero(number.substring(6, 8), "हजार") +
          handleZero(number.substring(8, 9), "सय") +
          numberBelow100Convertor(number.substring(9));

      // 1,23,45,67,89,000
      case 12 -> numberBelow100Convertor(number.substring(0, 1)) + " खर्ब " +
          handleZero(number.substring(1, 3), "अर्ब") +
          handleZero(number.substring(3, 5), "करोड") +
          handleZero(number.substring(5, 7), "लाख") +
          handleZero(number.substring(7, 9), "हजार") +
          handleZero(number.substring(9, 10), "सय") +
          numberBelow100Convertor(number.substring(10));

      default -> "";
    };
  }

  private static String handleZero(String number, String suffix) {
    var output = numberBelow100Convertor(number);
    if (Objects.equals(output, ""))
      return output;
    return output + " " + suffix + " ";
  }

  private static String numberBelow100Convertor(String number) {
    var length = number.length();
    if (length > 2)
      return "";

    return switch (number) {
      case "1", "01" -> "एक";
      case "2", "02" -> "दुई";
      case "3", "03" -> "तीन";
      case "4", "04" -> "चार";
      case "5", "05" -> "पाँच";
      case "6", "06" -> "छ";
      case "7", "07" -> "सात";
      case "8", "08" -> "आठ";
      case "9", "09" -> "नौ";
      case "10" -> "दश ";
      case "11" -> "एघार";
      case "12" -> "बाह्र";
      case "13" -> "तेह्र";
      case "14" -> "चौध";
      case "15" -> "पन्ध्र";
      case "16" -> "सोह्र";
      case "17" -> "सत्र";
      case "18" -> "अठार";
      case "19" -> "उन्नाइस";
      case "20" -> "विस";
      case "21" -> "एक्काइस";
      case "22" -> "बाइस";
      case "23" -> "तेईस";
      case "24" -> "चौविस";
      case "25" -> "पच्चिस";
      case "26" -> "छब्बिस";
      case "27" -> "सत्ताईस";
      case "28" -> "अठ्ठाईस";
      case "29" -> "उनन्तिस";
      case "30" -> "तिस";
      case "31" -> "एकत्तिस";
      case "32" -> "बत्तिस";
      case "33" -> "तेत्तिस";
      case "34" -> "चौँतिस";
      case "35" -> "पैँतिस";
      case "36" -> "छत्तिस";
      case "37" -> "सैतिस";
      case "38" -> "अड्तीस";
      case "39" -> "उन्नचालिस";
      case "40" -> "चालिस";
      case "41" -> "एकचालिस";
      case "42" -> "बयालिस";
      case "43" -> "त्रिचालिस";
      case "44" -> "चौवालिस";
      case "45" -> "पैतालिस";
      case "46" -> "छयालिस";
      case "47" -> "सड्चालिस";
      case "48" -> "अड्चालिस";
      case "49" -> "उन्नचास";
      case "50" -> "पचास";
      case "51" -> "एकाउन्न";
      case "52" -> "बाउन्न";
      case "53" -> "त्रिपन्न";
      case "54" -> "चौउन्न";
      case "55" -> "पचपन्न";
      case "56" -> "छपन्न";
      case "57" -> "सन्ताउन्न";
      case "58" -> "अन्ठाउन्न";
      case "59" -> "उन्नसाठी";
      case "60" -> "साठी";
      case "61" -> "एकसठी";
      case "62" -> "बैसठी";
      case "63" -> "त्रिसठी";
      case "64" -> "चौसठी";
      case "65" -> "पैसठी";
      case "66" -> "छैसठी";
      case "67" -> "सत्सठी";
      case "68" -> "अड्सठी";
      case "69" -> "उनान्सत्तरी";
      case "70" -> "सत्तरी";
      case "71" -> "एकहत्तर";
      case "72" -> "बहत्तर";
      case "73" -> "त्रिहत्तर";
      case "74" -> "चौहत्तर";
      case "75" -> "पचहत्तर";
      case "76" -> "छेहत्तर";
      case "77" -> "सतहत्तर";
      case "78" -> "अठहत्तर";
      case "79" -> "उनउन्नअसी";
      case "80" -> "असी";
      case "81" -> "एकासी";
      case "82" -> "बयासी";
      case "83" -> "त्रियासी";
      case "84" -> "चौरासी";
      case "85" -> "पचासी";
      case "86" -> "छयासी";
      case "87" -> "सतासी";
      case "88" -> "अठासी";
      case "89" -> "उनान्नब्बे";
      case "90" -> "नब्बे";
      case "91" -> "एकानब्बे";
      case "92" -> "बयानब्बे";
      case "93" -> "त्रियानब्बे";
      case "94" -> "चौरानब्बे";
      case "95" -> "पंचानब्बे";
      case "96" -> "छयानब्बे";
      case "97" -> "सन्तानब्बे";
      case "98" -> "अन्ठानब्बे";
      case "99" -> "उनान्सय";
      default -> "";
    };
  }
}
