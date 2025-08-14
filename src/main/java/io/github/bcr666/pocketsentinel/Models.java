package io.github.bcr666.pocketsentinel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

public record Config(PaySchedule paySchedule, java.util.List<Pocket> pockets) {
	@JsonCreator
	public Config(
		@JsonProperty("PaySchedule") PaySchedule paySchedule,
		@JsonProperty("Pockets") java.util.List<Pocket> pockets
	) {
		this.paySchedule = paySchedule == null ? new PaySchedule(LocalDate.now(), 14) : paySchedule;
		this.pockets = pockets == null ? java.util.List.of() : pockets;
	}
}

public record PaySchedule(LocalDate nextPaycheck, int payPeriodDays) {
	@JsonCreator
	public PaySchedule(
		@JsonProperty("NextPaycheck") LocalDate nextPaycheck,
		@JsonProperty("PayPeriodDays") Integer payPeriodDays
	) {
		this.nextPaycheck = nextPaycheck;
		this.payPeriodDays = payPeriodDays == null ? 14 : payPeriodDays;
	}
}

public record Pocket(
	String name,
	BigDecimal currentBalance,
	BigDecimal transferPerPaycheck,
	LocalDate nextDueDate,
	BillEstimate estimate,
	BigDecimal safetyBuffer
) {
	@JsonCreator
	public Pocket(
		@JsonProperty("Name") String name,
		@JsonProperty("CurrentBalance") BigDecimal currentBalance,
		@JsonProperty("TransferPerPaycheck") BigDecimal transferPerPaycheck,
		@JsonProperty("NextDueDate") LocalDate nextDueDate,
		@JsonProperty("Estimate") BillEstimate estimate,
		@JsonProperty("SafetyBuffer") BigDecimal safetyBuffer
	) {
		this.name = name == null ? "" : name;
		this.currentBalance = scale(currentBalance);
		this.transferPerPaycheck = scale(transferPerPaycheck);
		this.nextDueDate = nextDueDate;
		this.estimate = estimate == null ? new BillEstimate(EstimateMode.FIXED, BigDecimal.ZERO, java.util.List.of(), 3, BigDecimal.ZERO) : estimate;
		this.safetyBuffer = safetyBuffer == null ? BigDecimal.ZERO : scale(safetyBuffer);
	}

	private static BigDecimal scale(BigDecimal v) {
		return v == null ? BigDecimal.ZERO : v.setScale(2, RoundingMode.HALF_UP);
	}
}

public record BillEstimate(
	EstimateMode mode,
	BigDecimal fixedAmount,
	java.util.List<BigDecimal> recentBills,
	int useLastN,
	BigDecimal extraPercent
) {
	@JsonCreator
	public BillEstimate(
		@JsonProperty("Mode") EstimateMode mode,
		@JsonProperty("FixedAmount") BigDecimal fixedAmount,
		@JsonProperty("RecentBills") java.util.List<BigDecimal> recentBills,
		@JsonProperty("UseLastN") Integer useLastN,
		@JsonProperty("ExtraPercent") BigDecimal extraPercent
	) {
		this.mode = mode == null ? EstimateMode.FIXED : mode;
		this.fixedAmount = scale(fixedAmount == null ? BigDecimal.ZERO : fixedAmount);
		this.recentBills = recentBills == null ? java.util.List.of() : recentBills.stream().map(Models::scale).toList();
		this.useLastN = useLastN == null ? 3 : useLastN;
		this.extraPercent = extraPercent == null ? BigDecimal.ZERO : extraPercent.setScale(2, RoundingMode.HALF_UP);
	}
}

enum EstimateMode {
	FIXED("Fixed"),
	AVERAGE("Average"),
	MEDIAN("Median");

	private final String json;
	EstimateMode(String json) { this.json = json; }
	@JsonValue public String json() { return json; }
}

public record ResultRow(
	String name,
	LocalDate dueDate,
	BigDecimal expectedBill,
	int paychecksBeforeDue,
	BigDecimal projectedOnDue,
	BigDecimal safetyBuffer,
	boolean atRisk
) { }

class Models {
	static BigDecimal scale(BigDecimal v) {
		return v == null ? BigDecimal.ZERO : v.setScale(2, RoundingMode.HALF_UP);
	}
}
