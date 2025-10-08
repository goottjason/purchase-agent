const stepLabel = {
  "SAVE_PRODUCT": "1단계(DB)", "DOWNLOAD_IMAGE": "2단계(이미지)",
  "UPLOAD_IMAGE": "3단계(ESM업로드)", "REGISTER_CHANNELS": "4단계(채널등록)"
};

let currentPage = 1;
let pageSize = 20;

// 페이징 및 페이지당 개수 구현
function loadStatus(page=currentPage, size=pageSize) {
  $.get(`/process-status/list?page=${page}&size=${size}`, function(data) {
    let tbody = "";
    console.log(data);
    data.rows.forEach(function(row){
      // 배치ID 앞 6자리만 표시
      const shortBatchId = row.batchId ? row.batchId.substring(0, 6) : '-';
      // 재시도 버튼(1,2,3,4단계 실패시)
      const actionBtn = row.status === "FAIL" ? `
        <button class="retry-btn" data-batch-id="${row.batchId}" 
                                  data-product-code="${row.productCode}" 
                                  data-step="${row.step}">재시도</button>` : "";
      // 삭제 버튼 (X 표시)
      const deleteBtn = `
        <button class="delete-btn" data-batch-id="${row.batchId}" 
                                   data-product-code="${row.productCode}" title="삭제">×</button>`;
      // 메시지 포맷팅 적용
      const formattedMessage = formatMessage(row.message, row.batchId, row.productCode);

      tbody += `
        <tr>
          <td><span class="batch-id-short" title="${row.batchId}">${shortBatchId}</span></td>
          <td class="text-center">${row.productCode || '-'}</td>
          <td class="text-center">${stepLabel[row.step]||row.step}</td>
          <td class="text-center">${row.status}</td>
          <td>${formattedMessage}</td>
          <td class="text-center">${actionBtn} ${deleteBtn}</td>
        </tr>`;
    });
    $("#statusTable tbody").html(tbody);

    // 페이징 UI (버튼)
    let pagingHtml = "";
    for (let i = 1; i <= data.totalPages; i++) {
      pagingHtml += `
        <button class="paging-btn" data-page="${i}" 
                style="margin:0 2px;${i==page ? 'background:#2563eb;color:#fff;'
                                              : 'background:#eee;color:#333;'}">${i}</button>`;
    }
    $("#pagingArea").html(pagingHtml);
  });
}
// JSON 메시지를 보기 좋게 포맷하는 함수
function formatMessage(message, batchId, productCode) {

  if (message.startsWith('{') && message.endsWith('}')) {
    try {
      const channelResults = JSON.parse(message);
      let formattedHtml = '<div class="channel-results-row">';

      Object.entries(channelResults).forEach(([channel, resultList]) => {
        const channelName = {
          'coupang': '쿠팡',
          'smartstore': '스마트스토어',
          'elevenst': '11번가'
        }[channel] || channel;

        if (!Array.isArray(resultList)) resultList = [resultList];

        resultList.forEach(result => {
          const statusClass = result.status === 'SUCCESS' ? 'success' : 'fail';
          const typeLabel = result.type === 'price' ? '가격' :
            result.type === 'stock' ? '재고' : (result.type || '');
          const clickable = result.status === 'FAIL' ? 'cursor:pointer;' : '';
          const dataAttrs = result.status === 'FAIL'
            ? `data-channel="${channel}" data-batch-id="${batchId}" data-product-code="${productCode}"`
            : '';

          formattedHtml += `
            <div class="channel-result ${statusClass}" style="${clickable}" ${dataAttrs}>
              <strong>${channelName}${typeLabel ? ' ('+typeLabel+')' : ''}</strong>: ${result.status}
              <div class="channel-message">${result.message || ''}</div>
              ${result.channelProductId ? `<div class="product-id">상품ID: ${result.channelProductId}</div>` : ''}
            </div>
          `;
        });
      });

      formattedHtml += '</div>';
      return formattedHtml;
    } catch (e) {
      return message;
    }
  }
  return message;
}

$(document).ready(function(){
  loadStatus();

  // 페이지 버튼 클릭
  $(document).on("click", ".paging-btn", function() {
    currentPage = parseInt($(this).data("page"));
    loadStatus(currentPage, pageSize);
  });

  // 페이지당 건수 변경
  $("#pageSizeSelect").change(function() {
    pageSize = parseInt($(this).val());
    currentPage = 1;
    loadStatus(1, pageSize);
  });

  // X 버튼(row 삭제)
  $("#statusTable").on("click", ".delete-btn", function(){
    const batchId = $(this).data("batch-id");
    const productCode = $(this).data("product-code");
    if(confirm("정말 삭제하시겠습니까?")) {
      $.ajax({
        url: "/process-status/delete",
        method: "POST",
        contentType: "application/json",
        data: JSON.stringify({ batchId, productCode }),
        success: function(){
          alert("삭제 완료!");
          loadStatus(currentPage, pageSize);
        }
      });
    }
  });

  // 전체삭제
  $(".delete-all-btn").click(function() {
    if(confirm("전체 데이터를 정말 삭제하시겠습니까?")) {
      $.ajax({
        url: "/process-status/delete-all",
        method: "POST",
        success: function(){
          alert("전체 삭제 완료!");
          loadStatus(currentPage, pageSize);
        }
      });
    }
  });

  // 재시도 버튼 클릭
  $("#statusTable").on("click",".retry-btn",function(){
    const batchId = $(this).data("batch-id");
    const productCode = $(this).data("product-code");
    const step = $(this).data("step");

    $.ajax({
      url:"product-registration/retry",
      method:"POST",
      contentType:"application/json",
      data: JSON.stringify({ batchId, productCode, step }),
      success: function(){
        alert("재시도 완료!");
        loadStatus();
      }
    });
  });
  // 채널 결과 카드 클릭으로 재시도
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
        success: function(){
          alert("재시도 완료!");
          loadStatus();
        }
      });
    }
  });

});
