package com.dbtraining.reconx.dto;

import com.dbtraining.reconx.repository.entity.Counterparty;
import com.dbtraining.reconx.repository.entity.Instrument;
import com.dbtraining.reconx.repository.entity.Trade;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-08-02T17:12:32+0530",
    comments = "version: 1.6.2, compiler: javac, environment: Java 21.0.1 (Oracle Corporation)"
)
@Component
public class TradeMapperImpl implements TradeMapper {

    @Override
    public TradeResponse toResponse(Trade trade) {
        if ( trade == null ) {
            return null;
        }

        Long counterpartyId = null;
        String counterpartyName = null;
        Long instrumentId = null;
        String instrumentSymbol = null;
        String status = null;
        Long id = null;
        String tradeRef = null;
        String assetClass = null;
        String side = null;
        BigDecimal quantity = null;
        BigDecimal price = null;
        LocalDate tradeDate = null;
        Instant createdAt = null;
        Instant modifiedAt = null;

        counterpartyId = tradeCounterpartyId( trade );
        counterpartyName = tradeCounterpartyName( trade );
        instrumentId = tradeInstrumentId( trade );
        instrumentSymbol = tradeInstrumentSymbol( trade );
        status = TradeMapper.statusToString( trade.getStatus() );
        id = trade.getId();
        tradeRef = trade.getTradeRef();
        assetClass = trade.getAssetClass();
        side = trade.getSide();
        quantity = trade.getQuantity();
        price = trade.getPrice();
        tradeDate = trade.getTradeDate();
        createdAt = trade.getCreatedAt();
        modifiedAt = trade.getModifiedAt();

        TradeResponse tradeResponse = new TradeResponse( id, tradeRef, counterpartyId, counterpartyName, instrumentId, instrumentSymbol, assetClass, side, quantity, price, tradeDate, status, createdAt, modifiedAt );

        return tradeResponse;
    }

    @Override
    public Trade toEntity(TradeRequest request) {
        if ( request == null ) {
            return null;
        }

        Trade trade = new Trade();

        trade.setTradeRef( request.tradeRef() );
        trade.setQuantity( request.quantity() );
        trade.setPrice( request.price() );
        trade.setAssetClass( request.assetClass() );
        trade.setTradeDate( request.tradeDate() );
        trade.setSide( request.side() );

        return trade;
    }

    private Long tradeCounterpartyId(Trade trade) {
        Counterparty counterparty = trade.getCounterparty();
        if ( counterparty == null ) {
            return null;
        }
        return counterparty.getId();
    }

    private String tradeCounterpartyName(Trade trade) {
        Counterparty counterparty = trade.getCounterparty();
        if ( counterparty == null ) {
            return null;
        }
        return counterparty.getName();
    }

    private Long tradeInstrumentId(Trade trade) {
        Instrument instrument = trade.getInstrument();
        if ( instrument == null ) {
            return null;
        }
        return instrument.getId();
    }

    private String tradeInstrumentSymbol(Trade trade) {
        Instrument instrument = trade.getInstrument();
        if ( instrument == null ) {
            return null;
        }
        return instrument.getSymbol();
    }
}
