$(document).ready(function() {


  // 1. 선택 개수 표시 + 버튼 활성화
  function updateSelectedCount() {
    const count = $('.product-checkbox:checked').length;
    $('#selectedCount').text(count);
    $('#batch-update-btn').prop('disabled', count === 0);
  }
  $('.product-checkbox, #selectAll').on('change', updateSelectedCount);
  updateSelectedCount(); // 페이지 최초

// 2. "가격/재고 업데이트" 버튼 클릭 이벤트 → 데이터, 옵션 전송
  $('#batch-update-btn').on('click', function() {

    const selectedCodes = $('.product-checkbox:checked').map(function() {
      return $(this).val(); }).get();

    if (selectedCodes.length === 0) return;

    // 입력값 읽기
    const marginRate = Number($('#margin-rate').val());
    const couponRate = Number($('#coupon-rate').val());
    const minMarginPrice = Number($('#min-margin-price').val());

    // 체크 상품 정보 추출(코드 → 나머지 정보는 테이블 data-xxx에서 가져와도 되고 row에서 추가 가능)
    const products = selectedCodes.map(code => {
      const $row = $('input.product-checkbox[value="'+code+'"]').closest('tr');
      return {
        code: $row.data('code'),
        supplierCode: $row.data('supplier-code'),
        title: $row.data('title'),
        link: $row.data('link'),
        unitValue: $row.data('unit-value'),
        unit: $row.data('unit'),
        packQty: $row.data('pack-qty'),
        salePrice: $row.data('sale-price'),
        stock: $row.data('stock'),
        korName: $row.data('kor-name'),
        engName: $row.data('eng-name'),
        brandName: $row.data('brand-name'),
        vendorItemId: $row.data('vendor-item-id'),
        sellerProductId: $row.data('seller-product-id'),
        smartstoreId: $row.data('smartstore-id'),
        originProductNo: $row.data('origin-product-no'),
        elevenstId: $row.data('elevenst-id')
      };
    });

    // AJAX로 서버(MQ 또는 REST) 요청
    $.ajax({
      url: '/admin/products/batch-auto-price-stock-update',
      method: 'POST',
      contentType: 'application/json',
      data: JSON.stringify({
        marginRate, couponRate, minMarginPrice, products // 전체 정보 서버로
      }),
      success: function(resp) {
        alert('업데이트 요청이 성공적으로 MQ로 전송되었습니다.');
      },
      error: function(xhr) {
        alert('업데이트 요청 실패: ' + xhr.responseText);
      }
    });
  });



  // 페이지 로딩 직후 실행할 이벤트 등록 및 초기 상태 셋업
  // 1. DOM 조작 (체크박스, 셀편집 등)
  // 2. 버튼/셀이벤트, 동적 함수, Ajax 등

  // 수정된 데이터를 저장할 객체
  let modifiedData = {};

  // 페이지 로딩 시 활성 필터 표시
  updateActiveFilters();

  /**
   * 전체 선택/해제
   */
  $('#selectAll').change(function() {
    $('.product-checkbox').prop('checked', $(this).prop('checked'));
  });

  /**
   * 개별 체크박스 변경 시 전체 선택 체크박스 상태 업데이트
   */
  $('.product-checkbox').change(function() {
    const totalCheckboxes = $('.product-checkbox').length;
    const checkedCheckboxes = $('.product-checkbox:checked').length;
    $('#selectAll').prop('checked', totalCheckboxes === checkedCheckboxes);
    $('#selectAll').prop('indeterminate', checkedCheckboxes > 0 && checkedCheckboxes < totalCheckboxes);
  });

  /**
   * 편집 가능한 셀 더블클릭 이벤트
   */
  $('.editable-cell').dblclick(function() {
    const $cell = $(this);
    const code = $cell.closest('tr').data('code'); // $cell.data('code');
    console.log(code);
    const field = $cell.data('field');
    const currentValue = $cell.data('value') || $cell.text().replace(/,/g, '');

    // 이미 편집 중이면 무시
    if ($cell.hasClass('editing')) {
      return;
    }

    // 편집 모드 표시
    $cell.addClass('editing');

    // 입력 필드 생성
    const inputType = field === 'salePrice' || field === 'stock' ? 'number' : 'text';
    const $input = $('<input>', {
      type: inputType,
      class: 'editing-input',
      value: currentValue,
      'data-original': currentValue
    });

    // 셀 내용을 입력 필드로 교체
    $cell.html($input);
    $input.focus().select();

    // 엔터키 또는 포커스 아웃 시 편집 완료
    $input.on('blur keypress', function(e) {
      if (e.type === 'blur' || e.which === 13) {
        finishEditing($cell, $input, code, field);
      }
    });

    // ESC 키로 편집 취소
    $input.on('keydown', function(e) {
      if (e.which === 27) { // ESC
        cancelEditing($cell, $input);
      }
    });
  });

  /**
   * 편집 완료 처리
   */
  function finishEditing($cell, $input, code, field) {
    const newValue = $input.val();
    const originalValue = $input.data('original');

    // 값 유효성 검사
    if (field === 'salePrice' || field === 'stock') {
      if (isNaN(newValue) || newValue < 0) {
        alert('올바른 숫자를 입력해주세요.');
        $input.focus();
        return;
      }
    }

    // 편집 모드 해제
    $cell.removeClass('editing');

    // 값이 변경되었는지 확인
    if (newValue !== originalValue) {
      // 수정된 데이터에 추가
      if (!modifiedData[code]) {
        modifiedData[code] = { code: code };
        // 마켓 id를 셀/row의 data 속성에서 미리 읽어서 같이 담아둠
        modifiedData[code].sellerProductId = $cell.data('seller-product-id');
        modifiedData[code].vendorItemId = $cell.data('vendor-item-id');
        modifiedData[code].smartstoreId = $cell.data('smartstore-id');
        modifiedData[code].originProductNo = $cell.data('origin-product-no');
        modifiedData[code].elevenstId = $cell.data('elevenst-id');
      }
      modifiedData[code][field] = parseInt(newValue) || newValue;

      // 셀 스타일 변경 (수정됨 표시)
      $cell.addClass('modified-cell');

      // 저장 버튼 표시
      $('#saveBtn').show();
    }

    // 셀 내용 업데이트
    const displayValue = field === 'salePrice' ?
      parseInt(newValue).toLocaleString() : newValue;
    $cell.text(displayValue).data('value', newValue);
  }

  /**
   * 편집 취소 처리
   */
  function cancelEditing($cell, $input) {
    const originalValue = $input.data('original');
    $cell.removeClass('editing');

    const displayValue = $cell.data('field') === 'salePrice' ?
      parseInt(originalValue).toLocaleString() : originalValue;
    $cell.text(displayValue);
  }

  /**
   * 저장 버튼 클릭 이벤트
   */
  $('#saveBtn').click(function() {
    const modifiedProductCount = Object.keys(modifiedData).length;

    if (modifiedProductCount === 0) {
      alert('저장할 변경사항이 없습니다.');
      return;
    }

    // 확인 대화상자
    if (!confirm(`${modifiedProductCount}개 상품의 변경사항을 저장하시겠습니까?`)) {
      return;
    }

    // 로딩 표시
    $(this).prop('disabled', true).html('<i class="fas fa-spinner fa-spin"></i> 저장 중...');

    // 서버로 데이터 전송
    const updateData = {
      updateItems: Object.values(modifiedData)
    };

    $.ajax({
      url: '/products/bulk-update',
      method: 'POST',
      contentType: 'application/json',
      data: JSON.stringify(updateData),
      success: function(response) {
        if (response.success) {
          // 성공 메시지 표시
          showSaveIndicator('성공적으로 저장되었습니다.');

          // 수정된 데이터 초기화
          modifiedData = {};

          // 셀 스타일 초기화
          $('.modified-cell').removeClass('modified-cell');

          // 저장 버튼 숨기기
          $('#saveBtn').hide();
        } else {
          alert('저장 실패: ' + response.message);
        }
      },
      error: function(xhr, status, error) {
        alert('저장 중 오류가 발생했습니다: ' + error);
      },
      complete: function() {
        // 로딩 해제
        $('#saveBtn').prop('disabled', false).html('<i class="fas fa-save"></i> 변경사항 저장');
      }
    });
  });

  /**
   * 저장 완료 표시
   */
  function showSaveIndicator(message) {
    $('#saveIndicator').text(message).fadeIn().delay(3000).fadeOut();
  }

  /**
   * 페이지 크기 변경 시 자동 검색
   */
  $('#pageSize').change(function() {
    $('#searchForm').submit();
  });

  $('#sortOrder').change(function() {
    $('#searchForm').submit();
  });

});

/**
 * 공급업체 필터 옵션 검색
 */
function filterSupplierOptions(searchText) {
  const options = $('#supplierFilterOptions .filter-option[data-value!="all"]');
  options.each(function() {
    const text = $(this).text().toLowerCase();
    if (text.includes(searchText.toLowerCase())) {
      $(this).show();
    } else {
      $(this).hide();
    }
  });
}

/**
 * 공급업체 필터 적용
 */
function applySupplierFilter() {
  const selectedSuppliers = [];
  $('.supplier-filter-checkbox:checked').each(function() {
    selectedSuppliers.push($(this).closest('.filter-option').data('value'));
  });

  // 폼에 숨겨진 필드 업데이트
  $('#hiddenFilters input[name="supplierCodes"]').remove();
  selectedSuppliers.forEach(function(supplier) {
    $('#hiddenFilters').append(
      '<input type="hidden" name="supplierCodes" value="' + supplier + '">'
    );
  });

  // 필터 버튼 상태 업데이트
  const filterBtn = $('[data-filter="supplier"]');
  if (selectedSuppliers.length > 0) {
    filterBtn.addClass('filtered');
  } else {
    filterBtn.removeClass('filtered');
  }

  // 검색 실행
  $('input[name="pageNumber"]').val(0);
  $('#searchForm').submit();
}

/**
 * 공급업체 필터 초기화
 */
function resetSupplierFilter() {
  $('.supplier-filter-checkbox').prop('checked', false);
  $('#supplier-all').prop('checked', true);

  // 숨겨진 필드에서 공급업체 필터 제거
  $('#hiddenFilters input[name="supplierCodes"]').remove();

  // 필터 버튼 상태 업데이트
  $('[data-filter="supplier"]').removeClass('filtered');

  // 검색 실행
  $('input[name="pageNumber"]').val(0);
  $('#searchForm').submit();
}

/**
 * 채널 NULL 필터 토글
 */
function toggleNullFilter(channelType, isChecked) {
  // 숨겨진 필드 업데이트
  $(`input[name="filterNull${channelType.charAt(0).toUpperCase() + channelType.slice(1)}"]`).val(isChecked);

  // 필터 버튼 상태 업데이트
  const filterBtn = $(`[data-filter="${channelType}"]`);
  if (isChecked) {
    filterBtn.addClass('filtered');
  } else {
    filterBtn.removeClass('filtered');
  }

  // 검색 실행
  $('input[name="pageNumber"]').val(0);
  $('#searchForm').submit();
}

/**
 * 페이지 이동
 */
function goToPage(pageNumber) {
  $('input[name="pageNumber"]').val(pageNumber);
  $('#searchForm').submit();
}

/**
 * 전체 필터 초기화
 */
function resetAllFilters() {
  $('#searchKeyword').val('');
  $('#pageSize').val('50');
  $('input[name="pageNumber"]').val('0');

  // 공급업체 필터 초기화
  $('.supplier-filter-checkbox').prop('checked', false);
  $('#supplier-all').prop('checked', true);
  $('#hiddenFilters input[name="supplierCodes"]').remove();

  // 채널 필터 초기화
  $('input[name="filterNullVendorItemId"]').val('false');
  $('input[name="filterNullSellerProductId"]').val('false');
  $('input[name="filterNullSmartstoreId"]').val('false');
  $('input[name="filterNullElevenstId"]').val('false');

  // 필터 버튼 상태 초기화
  $('.filter-dropdown-btn').removeClass('filtered');

  $('#searchForm').submit();
}

/**
 * 활성 필터 표시 업데이트
 */
function updateActiveFilters() {
  const filterTags = [];

  // 검색어
  if (searchKeyword && searchKeyword !== 'null' && searchKeyword !== '') {
    filterTags.push(`검색: "${searchKeyword}"`);
  }

  // 공급업체 필터
  if (selectedSuppliers && selectedSuppliers.length > 0) {
    filterTags.push(`공급업체: ${selectedSuppliers.join(', ')}`);
  }

  // 채널 NULL 필터
  if (filterNullVendorItemId) filterTags.push('쿠팡 품목ID: NULL');
  if (filterNullSellerProductId) filterTags.push('쿠팡 상품ID: NULL');
  if (filterNullSmartstoreId) filterTags.push('스마트스토어ID: NULL');
  if (filterNullElevenstId) filterTags.push('11번가ID: NULL');

  console.log(filterTags);
  // 필터 태그 표시
  if (filterTags.length > 0) {
    let tagsHtml = '';
    filterTags.forEach(function(tag) {
      tagsHtml += `<span class="filter-tag">${tag} <span class="remove-filter" onclick="removeFilter('${tag}')">×</span></span>`;
    });
    $('#filterTags').html(tagsHtml);
    $('#activeFilters').show();
  } else {
    $('#activeFilters').hide();
  }


}
