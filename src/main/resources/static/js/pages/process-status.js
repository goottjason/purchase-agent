const stepLabel = {
  "SAVE_PRODUCT": "1단계(DB)", "DOWNLOAD_IMAGE": "2단계(이미지)",
  "UPLOAD_IMAGE": "3단계(ESM업로드)", "REGISTER_CHANNELS": "4단계(채널등록)",
  "BATCH_UPDATE": "배치 업데이트", "DB SAVE": "DB 저장", "CHANNEL UPDATE": "채널 동기화"
};

let currentPage = 1;
let pageSize = 20;

// 페이징 및 페이지당 개수 구현
function loadStatus(page=currentPage, size=pageSize) {
  $.get(`/process-status/list?page=${page}&size=${size}`, function(data) {
    let tbody = "";
    console.log("받은 데이터:", data);
    console.log("전체 row 개수:", data.rows.length);

    // batchId별로 그룹화
    const batchGroups = {};
    data.rows.forEach(function(row) {
      console.log("처리중인 row:", row);
      console.log("productCode 값:", row.productCode, "타입:", typeof row.productCode);

      if (!batchGroups[row.batchId]) {
        batchGroups[row.batchId] = { summary: null, details: [] };
      }

      // productCode가 null 또는 undefined 또는 빈 문자열이면 총괄 row
      if (row.productCode == null || row.productCode === '') {
        console.log("총괄 row로 분류:", row.batchId);
        batchGroups[row.batchId].summary = row;
      } else {
        console.log("개별 row로 분류:", row.batchId, row.productCode);
        batchGroups[row.batchId].details.push(row);
      }
    });

    console.log("그룹화된 배치:", batchGroups);
    console.log("배치 개수:", Object.keys(batchGroups).length);

    // 각 배치별로 총괄 row + 개별 상품 row 렌더링
    Object.keys(batchGroups).forEach(function(batchId, i) {
      const batch = batchGroups[batchId];
      console.log("렌더링 중인 배치:", batchId, "총괄:", batch.summary, "개별:", batch.details.length);

      // 총괄 row가 있으면 렌더링
      if (batch.summary) {
        const row = batch.summary;
        const shortBatchId = row.batchId ? row.batchId.substring(0, 6) : '-';
        const hasDetails = batch.details.length > 0;
        const toggleIcon = hasDetails ? '<span class="toggle-icon">▶</span>' : '';

        const deleteBtn = `
          <button class="delete-btn" data-batch-id="${row.batchId}" 
                                     data-product-code="" title="배치 전체 삭제">×</button>`;

        const retryBtn = row.status === "FAILED" || row.status === "PARTIAL_SUCCESS" ? `
          <button class="retry-btn batch-retry" data-batch-id="${row.batchId}">전체 재시도</button>` : "";

        tbody += `
          <tr class="batch-summary-row" data-batch-id="${row.batchId}">
            <td>
              ${toggleIcon}
              <span class="batch-id-short" title="${row.batchId}">${shortBatchId}</span>
            </td>
            <td class="text-center">총괄 (${batch.details.length}개)</td>
            <td class="text-center">${stepLabel[row.step]||row.step}</td>
            <td class="text-center">${row.status}</td>
            <td>${row.message || '-'}</td>
            <td class="text-center">${retryBtn} ${deleteBtn}</td>
          </tr>`;
      }

      // 개별 상품 row 렌더링 (총괄이 없어도 렌더링)
      batch.details.forEach(function(row) {
        const showRow = (i === 0); // 첫 번째 배치만 펼침
        const trClass = "product-detail-row" + (showRow ? " show" : "");
        const actionBtn = row.status === "FAIL" || row.status === "FAILED" ? `
          <button class="retry-btn" data-batch-id="${row.batchId}" 
                                    data-product-code="${row.productCode}" 
                                    data-step="${row.step}">재시도</button>` : "";

        const deleteBtn = `
          <button class="delete-btn" data-batch-id="${row.batchId}" 
                                     data-product-code="${row.productCode}" title="삭제">×</button>`;

        const formattedMessage = formatMessage(row.message, row.batchId, row.productCode);

        tbody += `
          <tr class="product-detail-row" data-batch-id="${row.batchId}">
            <td></td>
            <td class="text-center">${row.productCode || '-'}</td>
            <td class="text-center">${stepLabel[row.step]||row.step}</td>
            <td class="text-center">${row.status}</td>
            <td>${formattedMessage}</td>
            <td class="text-center">${actionBtn} ${deleteBtn}</td>
          </tr>`;
      });
    });

    console.log("생성된 tbody 길이:", tbody.length);
    console.log("tbody 내용 일부:", tbody.substring(0, 200));

    $("#statusTable tbody").html(tbody);
    console.log("테이블에 삽입 완료, 실제 tr 개수:", $("#statusTable tbody tr").length);

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

  // null 또는 undefined 체크
  if (!message) {
    return '-';
  }

  // 문자열로 변환 (혹시 모를 타입 오류 방지)
  message = String(message);

  if (message.startsWith('{') && message.endsWith('}')) {
    try {
      const channelResults = JSON.parse(message);
      let formattedHtml = '<div class="channel-results-row">';

      Object.entries(channelResults).forEach(([channel, resultList]) => {
        const channelName = {
          'coupang': '쿠팡',
          'smartstore': '스마트스토어',
          'elevenst': '11번가',
          'cafe': '카페24'

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
      console.error("메시지 파싱 오류:", e, "원본:", message);
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

  // 배치 총괄 row 클릭 시 개별 상품 row 토글
  $(document).on("click", ".batch-summary-row", function(e) {
    // 삭제 버튼이나 재시도 버튼 클릭 시 토글하지 않음
    if ($(e.target).hasClass('delete-btn') || $(e.target).hasClass('retry-btn')) {
      return;
    }

    const batchId = $(this).data("batch-id");
    const detailRows = $(`.product-detail-row[data-batch-id="${batchId}"]`);
    const toggleIcon = $(this).find(".toggle-icon");

    // 펼침/접힘 토글
    detailRows.toggleClass("show");
    toggleIcon.toggleClass("expanded");
  });

  // X 버튼(row 삭제)
  $("#statusTable").on("click", ".delete-btn", function(){
    const batchId = $(this).data("batch-id");
    const productCode = $(this).data("product-code");
    const confirmMsg = productCode ? "정말 삭제하시겠습니까?" : "배치 전체를 삭제하시겠습니까?";

    $.ajax({
      url: "/process-status/delete",
      method: "POST",
      contentType: "application/json",
      data: JSON.stringify({ batchId, productCode }),
      success: function(){
        // alert("삭제 완료!");
        loadStatus(currentPage, pageSize);
      }
    });

    /*if(confirm(confirmMsg)) {
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
    }*/
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

  // 재시도 버튼 클릭 (개별 상품)
  $("#statusTable").on("click",".retry-btn:not(.batch-retry)",function(){
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

  // 배치 전체 재시도 버튼 클릭
  $("#statusTable").on("click", ".batch-retry", function(){
    const batchId = $(this).data("batch-id");

    if(confirm("배치 전체를 재시도하시겠습니까?")) {
      $.ajax({
        url: "/products/batch/" + batchId + "/retry-failed",
        method: "POST",
        success: function(){
          alert("배치 재시도 완료!");
          loadStatus(currentPage, pageSize);
        },
        error: function(xhr) {
          alert("재시도 실패: " + (xhr.responseJSON?.message || "서버 오류"));
        }
      });
    }
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
