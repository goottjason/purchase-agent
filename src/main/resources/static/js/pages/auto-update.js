$(document).ready(function(){
  // 마진율, 쿠폰할인율, 최소마진, 총구매가격 입력시 예상 판매가 실시간 표시
  function updateEstimate() {
    // 총구매가격(입력값)
    const totalBuyPrice = parseFloat($("#totalPurchaseInput").val() || "0");
    // 마진율
    const marginRate = parseFloat($("#marginInput").val() || "0");
    // 쿠폰할인율
    const couponRate = parseFloat($("#couponInput").val() || "0");
    // 최소마진
    const minMarginPrice = parseInt($("#minMarginInput").val() || "0", 10);

    // 배송비: 4만원 미만시 추가
    let finalBuyPrice = totalBuyPrice;
    if (finalBuyPrice < 40000 && finalBuyPrice > 0) {
      finalBuyPrice += 6000;
    }

    // 기본 공식(쿠폰할인율은 buyPrice 반영/실제 계산로직은 개별상품 정보 필요)
    // 예시: basePrice = finalBuyPrice / ((100 - marginRate) / 100.0)
    let basePrice = finalBuyPrice / ((100 - marginRate) / 100.0);

    // 최소마진 보장
    let salePrice = Math.max(basePrice, minMarginPrice);

    // "xx,900원" 형식으로 변환
    salePrice = salePrice > 0
      ? Math.ceil(salePrice / 1000.0) * 1000 - 100
      : 0;

    // 안내메시지 갱신
    $("#priceEstimateMsg").text(
      "예상 판매가격은 " + (isNaN(salePrice) || salePrice <= 0 ? "[ ]" : salePrice + "원") + "입니다."
    );
  }

  $("#totalPurchaseInput,#marginInput,#couponInput,#minMarginInput").on('input', updateEstimate);

  updateEstimate();


  // 폼 제출 이벤트
  $("#batch-update-form").submit(function(e){
    e.preventDefault();
    // Ajax로 supplier, marginRate 넘겨서 서버에서 비동기로 배치 등록(rabbitMQ 메시지 생성)
    $.post('/api/auto-update', {
      supplierId: $("#supplierSelect").val(),
      marginRate: $("#marginInput").val(),
      couponRate: $("#couponInput").val(),
      minMarginPrice: $("#minMarginInput").val(),
      requestBy: "admin"
    }, function(resp){
      alert("업데이트 작업이 시작되었습니다."); // 성공 안내
    });
  });

  // 공급업체 셀렉트박스 옵션 동적 로딩
  $.get('/auto-update/suppliers', function(suppliers) {
    const $sel = $("#supplierSelect");
    $sel.empty(); // 혹시 이전 option 남아있으면 제거
    suppliers.forEach(s => {
      // Supplier 엔티티에 supplierCode, supplierName이 있다고 가정
      $sel.append($('<option>', {
        value: s.supplierCode,
        text: s.supplierName,
        selected: (s.supplierName && s.supplierName.toLowerCase().includes("iherb"))
      }));
    });
    // 만약 Iherb가 없다면 첫 번째 항목을 선택
    if ($sel.val() == null && $sel.children().length > 0) {
      $sel.val($sel.children().first().val());
    }
  });
});