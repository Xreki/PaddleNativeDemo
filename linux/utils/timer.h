#pragma once

#include <string>
#include <map>
#include <sstream>

#define REGISTER_TIMER(name)  \
  utils::g_utils_timer.add(name);              \
  utils::TimingUnit _timimg_##__LINE__(utils::g_utils_timer[name]->_seconds)

#define CLEAR_TIMER     \
  utils::g_utils_timer.clear()

#define PRINT_TIMER     \
  utils::g_utils_timer.print()

namespace utils {

template <class T>
std::string to_string(T value) {
    std::stringstream ss;
    ss << value;
    return ss.str();
}

class TimingUnit {
public:
  TimingUnit(double& seconds) : _seconds(seconds) {
    _start = get_current_time();
  }
  ~TimingUnit() {
    _seconds += (get_current_time() - _start);
  }

protected:
  double get_current_time();

protected:
  double& _seconds;
  double _start;
};

class TimerUnit {
public:
  TimerUnit() : _level(0), _id(0), _number_of_children(0), _seconds(0.0) {
    _parent = "";
  }
  TimerUnit(int level, int id, std::string parent)
    : _level(level),
      _id(id),
      _number_of_children(0),
      _seconds(0.0),
      _parent(parent) {}
  ~TimerUnit() {}

public:
  int _level;
  int _id;
  int _number_of_children;
  double _seconds;
  std::string _parent;
};

class Timer {
public:
  Timer() : _number_of_levels(0), _seconds(1.0E-15) {}
  ~Timer() {}

  void add(std::string name, std::string parent = "");

  std::string trans_to_string();

  void print();

  TimerUnit* operator [](const std::string& name) {
    if (_timer.find(name) != _timer.end()) {
      return &(_timer[name]);
    } else {
      return NULL;
    }
  }

  void clear() {
    _number_of_levels = 0;
    _seconds = 1.0E-15;
    _timer.clear();
  }

protected:
  std::string trans_to_string(int level, std::string parent);
  std::string trans_to_string_in_order(int level, std::string parent);

  void print_in_order(int level, std::string parent);

protected:
  int _number_of_levels;
  double _seconds;
  std::map<std::string, TimerUnit> _timer;
};

extern Timer g_utils_timer;

} // namespace utils
