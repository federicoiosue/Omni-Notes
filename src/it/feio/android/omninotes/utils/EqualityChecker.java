package it.feio.android.omninotes.utils;

public class EqualityChecker {
	public static synchronized boolean check(Object a, Object b) {
		boolean res = true;
		if (a != null) {
			res = res && a.equals(b);
		} else if (b != null) {
			res = res && b.equals(a);
		}
		return res;
	}

	public static synchronized boolean check(Object[] aArr, Object[] bArr) {
		boolean res = true;

		// Array size test
		res = res && aArr.length == bArr.length;

		// If arrays have the same length
		if (res) {
			for (int i = 0; i < aArr.length; i++) {
				Object a = aArr[i];
				Object b = bArr[i];

				// Content test on each element
				if (a != null) {
					res = res && a.equals(b);
				} else if (b != null) {
					res = res && b.equals(a);
				}
				
				// Exit if not equals
				if (!res)
					break;
			}
		}

		return res;
	}

}
