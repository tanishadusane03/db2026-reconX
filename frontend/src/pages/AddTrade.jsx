// TICKET-ADV123 — React Hook Form + Yup validation.
import React, { useState } from 'react';
import { useForm } from 'react-hook-form';
import { yupResolver } from '@hookform/resolvers/yup';
import * as yup from 'yup';
import { withAuth } from '@components/withAuth.jsx';
import { api } from '@services/apiService.js';

const today = new Date();

// Field names/types match TradeRequest (backend): tradeRef, instrumentId,
// counterpartyId, assetClass, side, quantity, price, tradeDate.
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
      await api.createTrade({
        ...values,
        tradeDate: values.tradeDate.toISOString().slice(0, 10),
      });
      reset();
      setSubmitted(true);
    } catch (err) {
      setSubmitError(err.message);
    }
  }

  return (
    <section>
      <h2>Add trade</h2>
      <form onSubmit={handleSubmit(onSubmit)} className="trade-form">
        <label>
          Trade ref
          <input {...register('tradeRef')} placeholder="EQU-20260603-0001" />
        </label>
        {errors.tradeRef && <span role="alert">{errors.tradeRef.message}</span>}

        <label>
          Instrument ID
          <input type="number" {...register('instrumentId')} placeholder="1-15 (seeded)" />
        </label>
        {errors.instrumentId && <span role="alert">{errors.instrumentId.message}</span>}

        <label>
          Counterparty ID
          <input type="number" {...register('counterpartyId')} placeholder="1-10 (seeded)" />
        </label>
        {errors.counterpartyId && <span role="alert">{errors.counterpartyId.message}</span>}

        <label>
          Asset class
          <select {...register('assetClass')}>
            <option value="EQUITY">EQUITY</option>
            <option value="FX">FX</option>
            <option value="BOND">BOND</option>
            <option value="DERIVATIVE">DERIVATIVE</option>
          </select>
        </label>
        {errors.assetClass && <span role="alert">{errors.assetClass.message}</span>}

        <label>
          Side
          <select {...register('side')}>
            <option value="BUY">BUY</option>
            <option value="SELL">SELL</option>
          </select>
        </label>
        {errors.side && <span role="alert">{errors.side.message}</span>}

        <label>
          Quantity
          <input type="number" step="any" {...register('quantity')} />
        </label>
        {errors.quantity && <span role="alert">{errors.quantity.message}</span>}

        <label>
          Price
          <input type="number" step="any" {...register('price')} />
        </label>
        {errors.price && <span role="alert">{errors.price.message}</span>}

        <label>
          Trade date
          <input type="date" {...register('tradeDate')} />
        </label>
        {errors.tradeDate && <span role="alert">{errors.tradeDate.message}</span>}

        {submitError && <div role="alert" className="form-error">{submitError}</div>}
        {submitted && <div role="status">Trade created.</div>}

        <button disabled={isSubmitting} type="submit">Submit</button>
      </form>
    </section>
  );
}

export default withAuth(AddTrade);
