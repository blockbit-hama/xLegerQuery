package blockbit.sql.util

class WCWidth {
  companion object {
    fun calculate(str: String): Int {
      return str.toCharArray().map { calculate(it.code) }.fold(0) { a, b -> a + b }
    }

    private fun calculate(ucs: Int): Int { /* test for 8-bit control characters */
      return when {
        ucs == 0 -> 0
        (ucs < 32 || ucs in 0x7f..159) -> -1
        /* binary search in table of non-spacing characters */
        bisearch(
          ucs,
          combining
        ) -> 0
        /* if we arrive here, ucs is not a combining or C0/C1 control character */
        else -> {
          1 + if (ucs >= 0x1100 &&
            (ucs <= 0x115f || /* Hangul Jamo init. consonants */
                ucs == 0x2329 || ucs == 0x232a ||
                ucs in 0x2e80..0xa4cf && ucs != 0x303f ||  /* CJK ... Yi */
                ucs in 0xac00..0xd7a3 ||  /* Hangul Syllables */
                ucs in 0xf900..0xfaff ||  /* CJK Compatibility Ideographs */
                ucs in 0xfe10..0xfe19 ||  /* Vertical forms */
                ucs in 0xfe30..0xfe6f ||  /* CJK Compatibility Forms */
                ucs in 0xff00..0xff60 ||  /* Fullwidth Forms */
                ucs in 0xffe0..0xffe6 ||
                ucs in 0x20000..0x2fffd ||
                ucs in 0x30000..0x3fffd)
          ) 1 else 0
        }
      }
    }

    var combining = arrayOf(
      Interval(0x0300, 0x036F),
      Interval(0x0483, 0x0486),
      Interval(0x0488, 0x0489),
      Interval(0x0591, 0x05BD),
      Interval(0x05BF, 0x05BF),
      Interval(0x05C1, 0x05C2),
      Interval(0x05C4, 0x05C5),
      Interval(0x05C7, 0x05C7),
      Interval(0x0600, 0x0603),
      Interval(0x0610, 0x0615),
      Interval(0x064B, 0x065E),
      Interval(0x0670, 0x0670),
      Interval(0x06D6, 0x06E4),
      Interval(0x06E7, 0x06E8),
      Interval(0x06EA, 0x06ED),
      Interval(0x070F, 0x070F),
      Interval(0x0711, 0x0711),
      Interval(0x0730, 0x074A),
      Interval(0x07A6, 0x07B0),
      Interval(0x07EB, 0x07F3),
      Interval(0x0901, 0x0902),
      Interval(0x093C, 0x093C),
      Interval(0x0941, 0x0948),
      Interval(0x094D, 0x094D),
      Interval(0x0951, 0x0954),
      Interval(0x0962, 0x0963),
      Interval(0x0981, 0x0981),
      Interval(0x09BC, 0x09BC),
      Interval(0x09C1, 0x09C4),
      Interval(0x09CD, 0x09CD),
      Interval(0x09E2, 0x09E3),
      Interval(0x0A01, 0x0A02),
      Interval(0x0A3C, 0x0A3C),
      Interval(0x0A41, 0x0A42),
      Interval(0x0A47, 0x0A48),
      Interval(0x0A4B, 0x0A4D),
      Interval(0x0A70, 0x0A71),
      Interval(0x0A81, 0x0A82),
      Interval(0x0ABC, 0x0ABC),
      Interval(0x0AC1, 0x0AC5),
      Interval(0x0AC7, 0x0AC8),
      Interval(0x0ACD, 0x0ACD),
      Interval(0x0AE2, 0x0AE3),
      Interval(0x0B01, 0x0B01),
      Interval(0x0B3C, 0x0B3C),
      Interval(0x0B3F, 0x0B3F),
      Interval(0x0B41, 0x0B43),
      Interval(0x0B4D, 0x0B4D),
      Interval(0x0B56, 0x0B56),
      Interval(0x0B82, 0x0B82),
      Interval(0x0BC0, 0x0BC0),
      Interval(0x0BCD, 0x0BCD),
      Interval(0x0C3E, 0x0C40),
      Interval(0x0C46, 0x0C48),
      Interval(0x0C4A, 0x0C4D),
      Interval(0x0C55, 0x0C56),
      Interval(0x0CBC, 0x0CBC),
      Interval(0x0CBF, 0x0CBF),
      Interval(0x0CC6, 0x0CC6),
      Interval(0x0CCC, 0x0CCD),
      Interval(0x0CE2, 0x0CE3),
      Interval(0x0D41, 0x0D43),
      Interval(0x0D4D, 0x0D4D),
      Interval(0x0DCA, 0x0DCA),
      Interval(0x0DD2, 0x0DD4),
      Interval(0x0DD6, 0x0DD6),
      Interval(0x0E31, 0x0E31),
      Interval(0x0E34, 0x0E3A),
      Interval(0x0E47, 0x0E4E),
      Interval(0x0EB1, 0x0EB1),
      Interval(0x0EB4, 0x0EB9),
      Interval(0x0EBB, 0x0EBC),
      Interval(0x0EC8, 0x0ECD),
      Interval(0x0F18, 0x0F19),
      Interval(0x0F35, 0x0F35),
      Interval(0x0F37, 0x0F37),
      Interval(0x0F39, 0x0F39),
      Interval(0x0F71, 0x0F7E),
      Interval(0x0F80, 0x0F84),
      Interval(0x0F86, 0x0F87),
      Interval(0x0F90, 0x0F97),
      Interval(0x0F99, 0x0FBC),
      Interval(0x0FC6, 0x0FC6),
      Interval(0x102D, 0x1030),
      Interval(0x1032, 0x1032),
      Interval(0x1036, 0x1037),
      Interval(0x1039, 0x1039),
      Interval(0x1058, 0x1059),
      Interval(0x1160, 0x11FF),
      Interval(0x135F, 0x135F),
      Interval(0x1712, 0x1714),
      Interval(0x1732, 0x1734),
      Interval(0x1752, 0x1753),
      Interval(0x1772, 0x1773),
      Interval(0x17B4, 0x17B5),
      Interval(0x17B7, 0x17BD),
      Interval(0x17C6, 0x17C6),
      Interval(0x17C9, 0x17D3),
      Interval(0x17DD, 0x17DD),
      Interval(0x180B, 0x180D),
      Interval(0x18A9, 0x18A9),
      Interval(0x1920, 0x1922),
      Interval(0x1927, 0x1928),
      Interval(0x1932, 0x1932),
      Interval(0x1939, 0x193B),
      Interval(0x1A17, 0x1A18),
      Interval(0x1B00, 0x1B03),
      Interval(0x1B34, 0x1B34),
      Interval(0x1B36, 0x1B3A),
      Interval(0x1B3C, 0x1B3C),
      Interval(0x1B42, 0x1B42),
      Interval(0x1B6B, 0x1B73),
      Interval(0x1DC0, 0x1DCA),
      Interval(0x1DFE, 0x1DFF),
      Interval(0x200B, 0x200F),
      Interval(0x202A, 0x202E),
      Interval(0x2060, 0x2063),
      Interval(0x206A, 0x206F),
      Interval(0x20D0, 0x20EF),
      Interval(0x302A, 0x302F),
      Interval(0x3099, 0x309A),
      Interval(0xA806, 0xA806),
      Interval(0xA80B, 0xA80B),
      Interval(0xA825, 0xA826),
      Interval(0xFB1E, 0xFB1E),
      Interval(0xFE00, 0xFE0F),
      Interval(0xFE20, 0xFE23),
      Interval(0xFEFF, 0xFEFF),
      Interval(0xFFF9, 0xFFFB),
      Interval(0x10A01, 0x10A03),
      Interval(0x10A05, 0x10A06),
      Interval(0x10A0C, 0x10A0F),
      Interval(0x10A38, 0x10A3A),
      Interval(0x10A3F, 0x10A3F),
      Interval(0x1D167, 0x1D169),
      Interval(0x1D173, 0x1D182),
      Interval(0x1D185, 0x1D18B),
      Interval(0x1D1AA, 0x1D1AD),
      Interval(0x1D242, 0x1D244),
      Interval(0xE0001, 0xE0001),
      Interval(0xE0020, 0xE007F),
      Interval(0xE0100, 0xE01EF)
    )

    class Interval(val first: Int, val last: Int)

    /* auxiliary function for binary search in interval table */
    private fun bisearch(ucs: Int, table: Array<Interval>): Boolean {
      var max = table.size - 1
      var min = 0
      var mid: Int
      if (ucs < table[0].first || ucs > table[max].last) return false
      while (max >= min) {
        mid = (min + max) / 2
        when {
          ucs > table[mid].last -> min = mid + 1
          ucs < table[mid].first -> max = mid - 1
          else -> return true
        }
      }
      return false
    }
  }
}