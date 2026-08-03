import React, { useState } from 'react';
import { useForm } from 'react-hook-form';
import { yupResolver } from '@hookform/resolvers/yup';
import * as yup from 'yup';
import { withAuth } from '@components/withAuth.jsx';
import { api } from '@services/apiService.js';
const schema = yup.object({ tradeRef: yup.string().matches(/^[A-Z]{3}-\d{8}-\d{4}$/, 'Format: AAA-YYYYMMDD-NNNN').required('Trade reference is required'), instrumentId: yup.number().typeError('Must be a number').integer().positive().required(), counterpartyId: yup.number().typeError('Must be a number').integer().positive().required(), assetClass: yup.string().oneOf(['EQUITY', 'FX', 'BOND', 'DERIVATIVE']).required(), side: yup.string().oneOf(['BUY', 'SELL']).required(), quantity: yup.number().typeError('Must be a number').positive().required(), price: yup.number().typeError('Must be a number').positive().required(), tradeDate: yup.date().typeError('Must be a valid date').required() });
function AddTrade() {
  const [serverError, setServerError] = useState(null);
  const { register, handleSubmit, formState: { errors, isSubmitting, isSubmitSuccessful }, reset } = useForm({ resolver: yupResolver(schema), mode: 'onBlur', defaultValues: { tradeRef: '', instrumentId: '', counterpartyId: '', assetClass: 'EQUITY', side: 'BUY', quantity: '', price: '', tradeDate: '' } });
  async function onSubmit(values) { setServerError(null); try { await api.createTrade(values); reset(); } catch (failure) { setServerError(failure.message || 'Failed to create trade'); } }
  const field = (name, label, type = 'text') => <><label>{label}<input type={type} step={type === 'number' ? '0.0001' : undefined} {...register(name)} /></label>{errors[name] && <p role="alert" className="form-error">{errors[name].message}</p>}</>;
  return <section><h2>Add trade</h2><form onSubmit={handleSubmit(onSubmit)} className="trade-form" noValidate>{field('tradeRef', 'Trade ref')}{field('instrumentId', 'Instrument id', 'number')}{field('counterpartyId', 'Counterparty id', 'number')}<label>Asset class<select {...register('assetClass')}><option value="EQUITY">EQUITY</option><option value="FX">FX</option><option value="BOND">BOND</option><option value="DERIVATIVE">DERIVATIVE</option></select></label><label>Side<select {...register('side')}><option value="BUY">BUY</option><option value="SELL">SELL</option></select></label>{field('quantity', 'Quantity', 'number')}{field('price', 'Price', 'number')}{field('tradeDate', 'Trade date', 'date')}{serverError && <p role="alert" className="form-error">{serverError}</p>}{isSubmitSuccessful && !serverError && <p role="status">Trade created.</p>}<button disabled={isSubmitting} type="submit">Submit</button></form></section>;
}
export default withAuth(AddTrade);
