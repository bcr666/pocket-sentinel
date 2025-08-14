package io.github.bcr666.pocketsentinel;

import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;

class Printer {
	static void print(List<ResultRow> rows) {
		rows.stream()
			.sorted(Comparator.comparing(ResultRow::dueDate).thenComparing(ResultRow::name))
			.forEach(r -> {
				String status = r.atRisk() ? "AT RISK" : "OK";
				System.out.printf(
					"%s\t%s\t$%s\t%d\t$%s\t$%s\t%s%n",
					r.name(),
					r.dueDate(),
					r.expectedBill().setScale(2, RoundingMode.HALF_UP),
					r.paychecksBeforeDue(),
					r.projectedOnDue().setScale(2, RoundingMode.HALF_UP),
					r.safetyBuffer().setScale(2, RoundingMode.HALF_UP),
					status
				);
			});
	}
}
