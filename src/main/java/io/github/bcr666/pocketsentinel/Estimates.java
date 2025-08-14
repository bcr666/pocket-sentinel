package io.github.bcr666.pocketsentinel;

import java.math.BigDecimal;
import java.math.RoundingMode;

class Estimates {
	static BigDecimal estimateBill(BillEstimate e) {
		if (e == null) return BigDecimal.ZERO;
		switch (e.mode()) {
			case FIXED -> {
				return scale(e.fixedAmount());
			}
			case AVERAGE -> {
				var seq = e.recentBills().stream().limit(Math.max(1, e.useLastN())).toList();
				if (seq.isEmpty()) return BigDecimal.ZERO;
				BigDecimal sum = seq.stream().map(Estimates::scale).reduce(BigDecimal.ZERO, BigDecimal::add);
				BigDecimal avg = sum.divide(BigDecimal.valueOf(seq.size()), 2, RoundingMode.HALF_UP);
				return applyExtra(avg, e.extraPercent());
			}
			case MEDIAN -> {
				var seq = e.recentBills().stream().limit(Math.max(1, e.useLastN()))
					.map(Estimates::scale).sorted().toList();
				if (seq.isEmpty()) return BigDecimal.ZERO;
				int n = seq.size();
				BigDecimal base = (n % 2 == 1)
					? seq.get(n / 2)
					: seq.get(n / 2 - 1).add(seq.get(n / 2)).divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);
				return applyExtra(base, e.extraPercent());
			}
			default -> {
				return BigDecimal.ZERO;
			}
		}
	}

	private static BigDecimal applyExtra(BigDecimal base, BigDecimal extraPercent) {
		if (extraPercent == null || extraPercent.compareTo(BigDecimal.ZERO) == 0) return base;
		return base.multiply(BigDecimal.ONE.add(extraPercent.divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP)))
			.setScale(2, RoundingMode.HALF_UP);
	}

	static BigDecimal scale(BigDecimal v) {
		return v == null ? BigDecimal.ZERO : v.setScale(2, RoundingMode.HALF_UP);
	}
}
