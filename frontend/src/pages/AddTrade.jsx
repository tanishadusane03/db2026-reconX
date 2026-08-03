// TICKET-ADV123 — React Hook Form + Yup validation.
import React, { useState } from 'react';
import { useForm } from 'react-hook-form';
import { yupResolver } from '@hookform/resolvers/yup';
import * as yup from 'yup';
import { withAuth } from '@components/withAuth.jsx';
import { api } from '@services/apiService.js';

const today = new Date();

const schema = yup.object({
  tradeRef: yup
    .string()
    .required('Trade ref is required')
    .matches(
      /^[A-Z]{3}-\d{8}-\d{4}$/,
      'Trade ref must match AAA-YYYYMMDD-NNNN'
    ),
  instrumentId: yup
    .number()
    .typeError('Instrument ID must be a number')
    .integer('Instrument ID must be an integer')
    .positive('Instrument ID must be positive')
    .required('Instrument ID is required'),
  counterpartyId: yup
    .number()
    .typeError('Counterparty ID must be a number')
    .integer('Counterparty ID must be an integer')
    .positive('Counterparty ID must be positive')
    .required('Counterparty ID is required'),
  assetClass: yup
    .string()
    .oneOf(['EQUITY', 'FX', 'BOND', 'DERIVATIVE'], 'Select an asset class')
    .required('Asset class is required'),
  side: yup
    .string()
    .oneOf(['BUY', 'SELL'], 'Select a side')
    .required('Side is required'),
  quantity: yup
    .number()
    .typeError('Quantity must be a number')
    .positive('Quantity must be positive')
    .required('Quantity is required'),
  price: yup
    .number()
    .typeError('Price must be a number')
    .positive('Price must be positive')
    .required('Price is required'),
  tradeDate: yup
    .date()
    .max(today, 'Trade date cannot be in the future')
    .required('Trade date is required'),
});

function AddTrade() {
  const [submitError, setSubmitError] = useState(null);
  const [submitted, setSubmitted] = useState(false);

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors, isSubmitting }
  } = useForm({
    resolver: yupResolver(schema),
    mode: 'onBlur',
    defaultValues: {
      tradeRef: '',
      instrumentId: '',
      counterpartyId: '',
      assetClass: 'EQUITY',
      side: 'BUY',
      quantity: '',
      price: '',
      tradeDate: ''
    }
  });

  async function onSubmit(values) {
    setSubmitError(null);
    setSubmitted(false);
    
    try {
      const tradeData = {
        tradeRef: values.tradeRef,
        instrumentId: Number(values.instrumentId),
        counterpartyId: Number(values.counterpartyId),
        assetClass: values.assetClass,
        side: values.side,
        quantity: Number(values.quantity),
        price: Number(values.price),
        tradeDate: values.tradeDate.toISOString().slice(0, 10)
      };
      
      console.log('📤 Sending trade data:', tradeData);
      
      const response = await api.createTrade(tradeData);
      
      console.log('✅ Trade created:', response);
      
      reset({
        tradeRef: '',
        instrumentId: '',
        counterpartyId: '',
        assetClass: 'EQUITY',
        side: 'BUY',
        quantity: '',
        price: '',
        tradeDate: ''
      });
      setSubmitted(true);
      
    } catch (err) {
      console.error('❌ Error:', err);
      setSubmitError(err.message || 'Failed to create trade');
    }
  }

  return (
    <>
      <div className="breadcrumb">
        <span>Home</span>
        <span className="separator">›</span>
        <span>Trades</span>
        <span className="separator">›</span>
        <span style={{ color: 'var(--color-text)' }}>Add Trade</span>
      </div>

      

      <div className="trade-page-layout" style={{ maxWidth: '1000px', margin: '0 auto' }}>
        {/* Main Form Container */}
        <div className="trade-form-container" style={{ gridColumn: '1 / -1' }}>
          <div className="section-title">Trade Details</div>
          <div className="section-subtitle">Enter the core trade information</div>

          <form onSubmit={handleSubmit(onSubmit)} className="trade-form">
            {/* Row 1: Trade Reference - Full width */}
            <div className="form-group" style={{ gridColumn: '1 / -1' }}>
              <label>
                Trade Reference <span className="required">*</span>
              </label>
              <input 
                {...register('tradeRef')} 
                placeholder="e.g. EQU-20260603-0001" 
              />
              {errors.tradeRef && (
                <span className="form-error" role="alert">{errors.tradeRef.message}</span>
              )}
            </div>

            {/* Row 2: Instrument ID + Counterparty ID */}
            <div className="form-row">
              <div className="form-group">
                <label>
                  Instrument ID <span className="required">*</span>
                </label>
                <input 
                  type="number" 
                  {...register('instrumentId')} 
                  placeholder="1-15 (seeded)" 
                />
                {errors.instrumentId && (
                  <span className="form-error" role="alert">{errors.instrumentId.message}</span>
                )}
              </div>

              <div className="form-group">
                <label>
                  Counterparty ID <span className="required">*</span>
                </label>
                <input 
                  type="number" 
                  {...register('counterpartyId')} 
                  placeholder="1-10 (seeded)" 
                />
                {errors.counterpartyId && (
                  <span className="form-error" role="alert">{errors.counterpartyId.message}</span>
                )}
              </div>
            </div>

            {/* Row 3: Asset Class + Side */}
            <div className="form-row">
              <div className="form-group">
                <label>
                  Asset Class <span className="required">*</span>
                </label>
                <select {...register('assetClass')}>
                  <option value="EQUITY">EQUITY</option>
                  <option value="FX">FX</option>
                  <option value="BOND">BOND</option>
                  <option value="DERIVATIVE">DERIVATIVE</option>
                </select>
                {errors.assetClass && (
                  <span className="form-error" role="alert">{errors.assetClass.message}</span>
                )}
              </div>

              <div className="form-group">
                <label>
                  Side <span className="required">*</span>
                </label>
                <select {...register('side')}>
                  <option value="BUY">BUY</option>
                  <option value="SELL">SELL</option>
                </select>
                {errors.side && (
                  <span className="form-error" role="alert">{errors.side.message}</span>
                )}
              </div>
            </div>

            {/* Row 4: Quantity + Price */}
            <div className="form-row">
              <div className="form-group">
                <label>
                  Quantity <span className="required">*</span>
                </label>
                <input 
                  type="number" 
                  step="any" 
                  {...register('quantity')} 
                  placeholder="e.g. 1000" 
                />
                {errors.quantity && (
                  <span className="form-error" role="alert">{errors.quantity.message}</span>
                )}
              </div>

              <div className="form-group">
                <label>
                  Price <span className="required">*</span>
                </label>
                <input 
                  type="number" 
                  step="any" 
                  {...register('price')} 
                  placeholder="e.g. 150.25" 
                />
                {errors.price && (
                  <span className="form-error" role="alert">{errors.price.message}</span>
                )}
              </div>
            </div>

            {/* Row 5: Trade Date - Full width */}
            <div className="form-group" style={{ gridColumn: '1 / -1' }}>
              <label>
                Trade Date <span className="required">*</span>
              </label>
              <input 
                type="date" 
                {...register('tradeDate')} 
              />
              {errors.tradeDate && (
                <span className="form-error" role="alert">{errors.tradeDate.message}</span>
              )}
            </div>

            {submitError && (
              <div className="form-error" role="alert" style={{ gridColumn: '1 / -1' }}>{submitError}</div>
            )}
            {submitted && (
              <div style={{ 
                gridColumn: '1 / -1',
                padding: '12px', 
                background: 'rgba(0, 184, 148, 0.1)', 
                borderRadius: 'var(--radius)',
                color: 'var(--color-success)',
                fontWeight: '500'
              }}>
                ✓ Trade created successfully!
              </div>
            )}

            <div className="form-actions" style={{ gridColumn: '1 / -1' }}>
              <button 
                type="button" 
                className="btn btn-secondary"
                onClick={() => {
                  reset({
                    tradeRef: '',
                    instrumentId: '',
                    counterpartyId: '',
                    assetClass: 'EQUITY',
                    side: 'BUY',
                    quantity: '',
                    price: '',
                    tradeDate: ''
                  });
                }}
              >
                Reset
              </button>
              <button 
                type="submit" 
                className="btn btn-primary"
                disabled={isSubmitting}
              >
                <i className="fas fa-plus-circle"></i>
                {isSubmitting ? 'Submitting...' : 'Submit'}
              </button>
            </div>
          </form>
        </div>
      </div>
    </>
  );
}

export default withAuth(AddTrade);