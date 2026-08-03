// TICKET-ADV123 — React Hook Form + Yup validation.
import React from 'react';
import { useForm } from 'react-hook-form';
import { yupResolver } from '@hookform/resolvers/yup';
import * as yup from 'yup';
import { withAuth } from '@components/withAuth.jsx';
import { api } from '@services/apiService.js';

// TODO(TICKET-ADV123): build a yup.object schema covering every field on the
//   form. Suggested validators:
//     tradeRef       — string, regex /^[A-Z]{3}-\d{8}-\d{4}$/ ("AAA-YYYYMMDD-NNNN")
//     instrumentId   — integer, positive
//     counterpartyId — integer, positive
//     assetClass     — oneOf ['EQUITY','FX','BOND','DERIVATIVE']
//     side           — oneOf ['BUY','SELL']
//     quantity       — positive number
//     price          — positive number
//     tradeDate      — date
const today = new Date();

const schema = yup.object({
  tradeRef: yup
    .string()
    .required('Trade ref is required')
    .matches(
      /^[A-Z]{3}-\d{8}-\d{4}$/,
      'Trade ref must match AAA-YYYYMMDD-NNNN'
    ),

  instrument: yup
    .string()
    .required('Instrument is required'),

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
      instrument: '',
      quantity: '',
      price: '',
      tradeDate: ''
    }
  });
  async function onSubmit(values) {
    await api.createTrade(values);
    reset();
  }

  return (
    <section>
      <h2>Add trade</h2>
      <form onSubmit={handleSubmit(onSubmit)} className="trade-form">
        {/* TODO(TICKET-ADV123): wire up <input {...register('tradeRef')} /> for
            every field listed in the schema above. Render
            errors.<field>.message under each input when present. */}
          <label>
  Trade ref
  <input 
    {...register('tradeRef')} 
    placeholder="EQU-20260603-0001"
  />
</label>

{errors.tradeRef && (
  <span role="alert">{errors.tradeRef.message}</span>
)}


<label>
  Instrument
  <input {...register('instrument')} />
</label>

{errors.instrument && (
  <span role="alert">{errors.instrument.message}</span>
)}


<label>
  Quantity
  <input type="number" {...register('quantity')} />
</label>

{errors.quantity && (
  <span role="alert">{errors.quantity.message}</span>
)}


<label>
  Price
  <input type="number" {...register('price')} />
</label>

{errors.price && (
  <span role="alert">{errors.price.message}</span>
)}


<label>
  Trade Date
  <input type="date" {...register('tradeDate')} />
</label>

{errors.tradeDate && (
  <span role="alert">{errors.tradeDate.message}</span>
)}
        <label>Trade ref   <input {...register('tradeRef')} placeholder="EQU-20260603-0001" /></label>
        {errors.tradeRef && <p className="form-error">{errors.tradeRef.message}</p>}

        <button disabled={isSubmitting} type="submit">Submit</button>
      </form>
    </section>
  );
}

export default withAuth(AddTrade);
