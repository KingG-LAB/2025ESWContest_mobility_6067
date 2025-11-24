def decide_avoid_dir(current_lane: int, total_lanes: int = 3) -> tuple[int, int]:
    """
    현재 차선의 정보를 따라 응급 차량 회피 메뉴얼에 따른 회피로직
    """
    if total_lanes == 3:
        if current_lane == 1:
            avoid_dir = 0   # 직진
        elif current_lane == 2:
            avoid_dir = 1   # 오른쪽
        elif current_lane == 3:
            avoid_dir = 0   # 직진
        else:
            avoid_dir = -1  # 판단 불가

        ambulance_lane = 2
        return avoid_dir, ambulance_lane
    
    elif total_lanes == 2:
        if current_lane == 1:
            avoid_dir = 1   # 오른쪽
        elif current_lane == 2:
            avoid_dir = 0   # 직진
        else:
            avoid_dir = -1  # 판단 불가

        ambulance_lane = 1
        return avoid_dir, ambulance_lane

    elif total_lanes == 1:
        if current_lane == 1:
            avoid_dir = 0   # 직진
        else:
            avoid_dir = -1  # 판단 불가

        ambulance_lane = 1
        return avoid_dir, ambulance_lane
    
    else:
        return -1,-1
