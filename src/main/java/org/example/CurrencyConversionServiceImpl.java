package org.example;


import java.util.HashMap;
import java.util.Map;
import io.grpc.stub.StreamObserver;

public class CurrencyConversionServiceImpl extends CurrencyConversionGrpc.CurrencyConversionImplBase {

    private static final Map<String, Double> conversionRates = new HashMap<>();

    static {
        conversionRates.put("USD", 1.0);
        conversionRates.put("EUR", 0.85);
        conversionRates.put("GBP", 0.75);
        conversionRates.put("RUB", 0.5);
        conversionRates.put("YUN", 0.65);

    }

    @Override
    public void convertCurrency(CurrencyConverter.CurrencyRequest request,
                                StreamObserver<CurrencyConverter.CurrencyResponse> responseObserver) {
        double convertedAmount = convert(request.getSourceCurrency(), request.getTargetCurrency(), request.getAmount());

        CurrencyConverter.CurrencyResponse response = CurrencyConverter.CurrencyResponse.newBuilder()
                .setConvertedAmount(convertedAmount)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    private double convert(String sourceCurrency, String targetCurrency, double amount) {
        double rate = getConversionRate(sourceCurrency, targetCurrency);
        return rate * amount;
    }

    private double getConversionRate(String sourceCurrency, String targetCurrency) {
        if (sourceCurrency.equals(targetCurrency)) {
            return 1.0;
        }

        Double sourceRate = conversionRates.get(sourceCurrency);
        Double targetRate = conversionRates.get(targetCurrency);

        if (sourceRate == null || targetRate == null) {
            throw new RuntimeException("No conversion rate available for one or both of the currencies.");
        }

        return  sourceRate/targetRate;
    }
}