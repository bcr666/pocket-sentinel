package io.github.bcr666.pocketsentinel;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class Main {
	public static void main(String[] args) {
		String file = args.length > 0 ? args[0] : "pockets.json";
		if (!Files.exists(Path.of(file))) {
			System.err.println("Missing " + file + ". Create one (see sample).");
			System.exit(2);
		}

		ObjectMapper mapper = new ObjectMapper()
			.registerModule(new JavaTimeModule())
			.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

		Config cfg;
		try {
			cfg = mapper.readValue(Files.readAllBytes(Path.of(file)), Config.class);
		} catch (IOException e) {
			System.err.println("Failed to read " + file + ": " + e.getMessage());
			System.exit(2);
			return;
		}

		List<ResultRow> rows = new ArrayList<>();
		for (Pocket p : cfg.pockets()) {
			int paychecks = countPaychecks(cfg.paySchedule().nextPaycheck(), cfg.paySchedule().payPeriodDays(), p.nextDueDate());
			BigDecimal expected = Estimates.estimateBill(p.estimate());
			BigDecimal projected = scale(p.currentBalance())
				.add(scale(p.transferPerPaycheck()).multiply(BigDecimal.valueOf(paychecks)))
				.subtract(expected);
			boolean atRisk = projected.compareTo(scale(p.safetyBuffer())) < 0;

			rows.add(new ResultRow(
				p.name(),
				p.nextDueDate(),
				expected,
				paychecks,
				projected,
				scale(p.safetyBuffer()),
				atRisk
			));
		}

		Printer.print(rows);
		boolean anyRisk = rows.stream().anyMatch(ResultRow::atRisk);
		System.exit(anyRisk ? 1 : 0);
	}

	private static int countPaychecks(LocalDate nextPaycheck, int periodDays, LocalDate dueDate) {
		if (nextPaycheck == null || dueDate == null) return 0;
		if (nextPaycheck.isAfter(dueDate)) return 0;
		long days = ChronoUnit.DAYS.between(nextPaycheck, dueDate);
		long periods = Math.floorDiv((int)days, periodDays);
		return (int)periods + 1; // include paycheck on dueDate
	}

	static BigDecimal scale(BigDecimal v) {
		return v == null ? BigDecimal.ZERO : v.setScale(2, RoundingMode.HALF_UP);
	}
}
