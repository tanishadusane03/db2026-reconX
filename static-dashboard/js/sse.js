// TICKET-ADV104 / TICKET-ADV105 — EventSource live feed with prepend + slide-in animation.

(function () {

  const feed = document.getElementById('trade-feed');
  if (!feed) return;


  const STREAM_URL = '/api/v1/trades/stream';

  let sse = null;


  const statusBadge = document.getElementById('sse-status');


  function updateConnectionBadge(text) {

    if (!statusBadge) return;

    statusBadge.textContent = text;

  }


  function escapeHtml(value) {
  return String(value)
    .replaceAll('&', '&amp;')
    .replaceAll('<', '&lt;')
    .replaceAll('>', '&gt;')
    .replaceAll('"', '&quot;')
    .replaceAll("'", '&#039;');
}


function formatQty(value) {
  return new Intl.NumberFormat('en-US').format(value);
}


function formatPrice(value) {
  return new Intl.NumberFormat('en-US', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 4
  }).format(value);
}


function prepend(trade) {

  let statusModifier = '';

  if (trade.status === 'MATCHED') {
    statusModifier = 'trade-card--matched';
  } 
  else if (trade.status === 'UNMATCHED') {
    statusModifier = 'trade-card--break';
  }


  const row = document.createElement('article');

  row.className =
    `trade-card ${statusModifier} trade-card--new`;


  row.innerHTML = `
    <header class="trade-card__header">
      <strong>${escapeHtml(trade.tradeRef)}</strong>
      <span>${escapeHtml(trade.status)}</span>
    </header>

    <div class="trade-card__body">
      <span>${escapeHtml(trade.symbol)}</span>
      <span>Qty: ${formatQty(trade.qty)}</span>
      <span>Price: ${formatPrice(trade.price)}</span>
      <span>${escapeHtml(trade.currency ?? '')}</span>
    </div>
  `;


  feed.prepend(row);


  setTimeout(() => {
    row.classList.remove('trade-card--new');
  }, 500);



  while (feed.children.length > 50) {
    feed.lastElementChild.remove();
  }

}



  function connect() {

    sse = new EventSource(STREAM_URL);


    // Connection opened successfully
    sse.onopen = function () {

      updateConnectionBadge('Live');

    };



    // New trade received from backend
    sse.onmessage = function (event) {

      try {

        const trade = JSON.parse(event.data);

        prepend(trade);

      } catch (error) {

        console.error('Invalid SSE message:', error);

      }

    };



    // Connection lost
    sse.onerror = function () {

      updateConnectionBadge('Reconnecting…');

      // IMPORTANT:
      // Do NOT create a new EventSource here.
      // Browser automatically reconnects.

    };

  }



  // Close SSE connection when leaving page
  window.addEventListener('beforeunload', function () {

    sse?.close();

  });



  // Start connection once when page loads
  updateConnectionBadge('Connecting…');

  connect();


})();