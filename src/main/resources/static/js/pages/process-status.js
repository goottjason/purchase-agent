const stepLabel = {
  "SAVE_PRODUCT": "1단계(DB)", "DOWNLOAD_IMAGE": "2단계(이미지)",
  "UPLOAD_IMAGE": "3단계(ESM업로드)", "REGISTER_CHANNELS": "4단계(채널등록)"
};

function loadStatus() {
  $.get('/product-registration/status', function(data) {
    let tbody = "";
    let showBatchRetryBtn = false;
    let batchProductCodes = [];
    let batchIdForUpload = null;
    console.log(data);

    data.forEach(function(row){
      let trClass = (row.status === "FAIL") ? "failed" : "";

      // 3단계 실패 상품은 체크박스 활성화
      let cb = "";
      if(row.step === "UPLOAD_IMAGE" && row.status === "FAIL") {
        cb = `<input type="checkbox" class="chk-upload-retry" data-batch-id="${row.batchId}" data-product-code="${row.productCode}">`;
        showBatchRetryBtn = true;
        batchProductCodes.push(row.productCode);
        batchIdForUpload = row.batchId;
      }

      // 4단계 채널별 실패 체크박스 UI
      /*let channelUI = "";
      if(row.step === "REGISTER_CHANNELS" && row.status === "FAIL" && row.details) {
        let details = {};
        try { details = JSON.parse(row.details); } catch(e) {}
        channelUI = `<div>
        <label><input type="checkbox" class="retry-channel" value="coupang"
        ${details.coupang && details.coupang.status==="FAIL"?'checked':''}>쿠팡</label>
        <label><input type="checkbox" class="retry-channel" value="smartstore"
        ${details.smartstore && details.smartstore.status==="FAIL"?'checked':''}>스마트스토어</label>
        <label><input type="checkbox" class="retry-channel" value="elevenst"
        ${details.elevenst && details.elevenst.status==="FAIL"?'checked':''}>11번가</label>
        </div>`;
      }*/

      // 단건 재시도 버튼(1,2단계 or 4단계 실패)
      let retryBtn = "";
      if(row.status === "FAIL" && (row.step!=="UPLOAD_IMAGE")) {
        retryBtn = `<button class="retry-btn"
          data-batch-id="${row.batchId}"
          data-product-code="${row.productCode}"
          data-step="${row.step}">
          재시도 [${stepLabel[row.step]||row.step}]
        </button>`;
      }

      // 메시지 포맷팅 적용
      const formattedMessage = formatMessage(row.message);

      tbody += `<tr class="${trClass}">
        <td>${cb}</td>
        <td>${row.batchId}</td>
        <td class="text-center">${row.productCode == null ? '-' : row.productCode}</td>
        <td>${stepLabel[row.step]||row.step}</td>
        <td>${row.status}</td>
        <td>${formattedMessage}</td>
        <td>${retryBtn}</td>
      </tr>`;
    });

    $("#statusTable tbody").html(tbody);
    // 3단계 실패 상품이 한 개 이상일 때만 일괄재시도 버튼 노출
    $("#batch-upload-retry").toggle(showBatchRetryBtn);
  });
}

// JSON 메시지를 보기 좋게 포맷하는 함수
function formatMessage(message, batchId, productCode) {
  // JSON 형태인지 확인
  if (message.startsWith('{') && message.endsWith('}')) {
    try {
      const channelResults = JSON.parse(message);
      let formattedHtml = '<div class="channel-results-row">';

      Object.entries(channelResults).forEach(([channel, result]) => {
        const channelName = {
          'coupang': '쿠팡',
          'smartstore': '스마트스토어',
          'elevenst': '11번가'
        }[channel] || channel;

        const statusClass = result.status === 'SUCCESS' ? 'success' : 'fail';
        // FAIL 카드만 클릭 이벤트 및 데이터 속성 부여
        const clickable = result.status === 'FAIL' ? 'cursor:pointer;' : '';
        const dataAttrs = result.status === 'FAIL'
          ? `data-channel="${channel}" data-batch-id="${batchId}" data-product-code="${productCode}"`
          : '';

        formattedHtml += `
          <div class="channel-result ${statusClass}" style="${clickable}" ${dataAttrs}>
            <strong>${channelName}</strong>: ${result.status}
            <div class="channel-message">${result.message}</div>
            ${result.channelProductId ? `<div class="product-id">상품ID: ${result.channelProductId}</div>` : ''}
          </div>
        `;
      });

      formattedHtml += '</div>';
      return formattedHtml;
    } catch (e) {
      return message; // JSON 파싱 실패시 원본 반환
    }
  }
  return message; // JSON이 아니면 원본 반환
}



$(document).ready(function(){
  loadStatus();
  // setInterval(loadStatus, 5000);

  // 3단계 이미지 일괄 재시도
  $("#batch-upload-retry").click(function(){
    // 체크된 상품코드만 추출해서 재시도 요청
    let batchId = null, productCodes = [];
    $(".chk-upload-retry:checked").each(function(){
      batchId = batchId || $(this).data("batch-id"); // 모든 상품의 batchId가 같아야 함!
      productCodes.push($(this).data("product-code"));
    });
    if(productCodes.length===0) {
      alert("3단계 실패 상품을 체크하세요!"); return;
    }
    $.ajax({
      url:"product-registration/retry",
      method:"POST",
      contentType:"application/json",
      data: JSON.stringify({ batchId, step: "UPLOAD_IMAGE", productCodes }),
      success: function(){ alert("일괄 재시도 완료!"); loadStatus(); }
    });
  });

  // 나머지(1,2,4단계) row별 재시도
  $("#statusTable").on("click",".retry-btn",function(){
    const batchId = $(this).data("batch-id");
    const productCode = $(this).data("product-code");
    const step = $(this).data("step");
    let retryChannels = [];
    if(step === "REGISTER_CHANNELS") {
      $(this).closest("tr").find(".retry-channel:checked").each(function(){
        retryChannels.push($(this).val());
      });
      if(retryChannels.length === 0) { alert("실패 채널을 선택하세요!"); return; }
    }
    $.ajax({
      url:"product-registration/retry",
      method:"POST",
      contentType:"application/json",
      data: JSON.stringify({ batchId, productCode, step, retryChannels }),
      success: function(){ alert("재시도 완료!"); loadStatus(); }
    });
  });


  $(document).on('click', '.channel-result.fail', function() {
    const channel = $(this).data('channel');
    const batchId = $(this).data('batch-id');
    const productCode = $(this).data('product-code');
    if (confirm(`${channel} 채널 등록을 재시도 하시겠습니까?`)) {
      $.ajax({
        url: "product-registration/retry",
        method: "POST",
        contentType: "application/json",
        data: JSON.stringify({
          batchId,
          productCode,
          step: "REGISTER_CHANNELS",
          retryChannels: [channel]
        }),
        success: function(){ alert("재시도 완료!"); loadStatus(); }
      });
    }
  });


});